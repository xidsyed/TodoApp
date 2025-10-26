package com.example.todoapp.core.cache.data

import com.example.todoapp.core.cache.*
import com.example.todoapp.core.cache.data.entity.KvCacheEntity
import com.example.todoapp.core.cache.data.model.CacheValue
import com.example.todoapp.core.serializer.Serializer
import com.github.benmanes.caffeine.cache.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.await
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.time.*
import java.util.concurrent.CompletableFuture

class PersistedCacheImpl<K : Any, V : Any>(
	private val persistence: CachePersistenceRepository,
	val cacheId: String,
	private val keySerializer: Serializer<K>,
	private val valueSerializer: Serializer<V>,
	defaultDuration: Duration,
	val cacheSize: Long = 100_000L,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PersistedCache<K, V> {

	private val scope = CoroutineScope(dispatcher + SupervisorJob())
	private val logger = LoggerFactory.getLogger(this.javaClass)
	private val _defaultDuration = defaultDuration
	private val caffeineExpiry = object : Expiry<K, CacheValue<V>> {
		override fun expireAfterCreate(key: K, value: CacheValue<V>, currentTime: Long): Long =
			value.duration?.toNanos() ?: _defaultDuration.toNanos()

		override fun expireAfterUpdate(key: K, value: CacheValue<V>, currentTime: Long, currentDuration: Long): Long =
			value.duration?.toNanos() ?: _defaultDuration.toNanos()

		override fun expireAfterRead(key: K, value: CacheValue<V>, currentTime: Long, currentDuration: Long): Long =
			currentDuration

	}

	private val caffeineRemovalListener = RemovalListener<K, CacheValue<V>> { key, _, cause ->
		when (cause) {
			RemovalCause.EXPLICIT, RemovalCause.REPLACED, RemovalCause.COLLECTED -> {
				// do nothing since explicit removals only take place within the class
				// persistance sync has already been taken care of
			}

			RemovalCause.EXPIRED -> {
				key?.let {
					scope.launch { persistence.deleteById(keySerializer.serialize(it), cacheId) }
				}
			}

			RemovalCause.SIZE -> {
				// TODO : Formalize this warning mechanism, and look into how this should be handled professionally
				logger.error("Persisted Cache $cacheId with limit of $cacheSize overflowed")
			}
		}
	}

	private val cache = Caffeine.newBuilder()
		.maximumSize(cacheSize)
		.expireAfter(caffeineExpiry)
		.removalListener(caffeineRemovalListener)
		.buildAsync<K, CacheValue<V>>()

	init {
		attemptCacheRestore()
	}

	override suspend fun put(k: K, v: V, duration: Duration?) {
		val future = CompletableFuture<CacheValue<V>>()
		// insert incomplete future early to avoid race conditions
		cache.put(k, future)
		persistence.upsert(createEntity(k, v, duration))
		future.complete(CacheValue(v, duration))
	}

	override suspend fun putIfAbsent(k: K, v: V, duration: Duration?) {
		if (cache.getIfPresent(k) != null) return
		else put(k, v, duration)
	}

	override suspend fun get(k: K): V? = cache.getIfPresent(k)?.await()?.value
	
	override fun getAll(): Flow<Pair<K, V>> = cache.asMap().entries.asFlow()
		.map { (key, futureValue) ->
			key to futureValue.await().value
		}

	override suspend fun clearCache() {
		persistence.deleteAllByCacheId(cacheId)
		cache.asMap().clear()
	}

	override suspend fun remove(k: K) {
		if (cache.getIfPresent(k) == null) return
		persistence.deleteById(keySerializer.serialize(k), cacheId)
		cache.asMap().remove(k)
	}

	private fun attemptCacheRestore() {
		scope.launch {
			persistence.deleteExpiredByCache(cacheId)                	// ensure all expired entries have been deleted
			persistence.findByCache(cacheId).map { entity ->        	// restore cache from persistance
				keySerializer.deserialize(entity.key) to entity.toCacheValue()
			}.collect { (key, value) ->
				cache.asMap().compute(key) { _, existingValue ->
					// only over-write if mapping is absent
					return@compute existingValue ?: CompletableFuture.completedFuture(value)
				}
			}
		}
	}

	private suspend fun createEntity(key: K, value: V, duration: Duration?): KvCacheEntity {
		val (key, value) = withContext(Dispatchers.Default) {
			val key = keySerializer.serialize(key)
			val value = valueSerializer.serialize(value)
			key to value
		}
		return KvCacheEntity(
			key = key,
			value = value,
			cache = cacheId,
			expireAt = OffsetDateTime.now().plusSeconds(duration?.seconds ?: _defaultDuration.seconds)
				.toInstant(),
		)
	}

	private fun KvCacheEntity.toCacheValue(): CacheValue<V> {
		val expireAtSeconds = expireAt.epochSecond
		val secondsNow = Clock.System.now().epochSeconds
		val remainingDuration = Duration.ofSeconds(expireAtSeconds - secondsNow)
		return CacheValue(valueSerializer.deserialize(value), remainingDuration)
	}
}