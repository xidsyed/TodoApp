package com.example.todoapp.app.cache.data

import com.example.todoapp.app.cache.data.entity.KvCacheEntity
import com.example.todoapp.app.cache.data.model.CacheValue
import com.example.todoapp.app.cache.domain.*
import com.example.todoapp.common.serialize.Serializer
import com.github.benmanes.caffeine.cache.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.future.await
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


class PersistedCacheImpl<K : Any, V : Any>(
	private val persistence: CachePersistenceRepository,
	val cacheId: String,
	private val keySerializer: Serializer<K>,
	private val valueSerializer: Serializer<V>,
	defaultDuration: Duration? = 24.hours,
	val cacheSize: Long = 1000_000L,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PersistedCache<K, V> {

	private val scope = CoroutineScope(dispatcher + SupervisorJob())
	private val logger = LoggerFactory.getLogger(this.javaClass)
	private val _defaultDuration = defaultDuration ?: 24.hours

	private val cache = Caffeine.newBuilder()
		.maximumSize(cacheSize)
		.expireAfter(object : Expiry<K, CacheValue<V>> {
			override fun expireAfterCreate(key: K, value: CacheValue<V>, currentTime: Long): Long =
				value.duration?.inWholeNanoseconds ?: _defaultDuration.inWholeNanoseconds

			override fun expireAfterUpdate(
				key: K,
				value: CacheValue<V>,
				currentTime: Long,
				currentDuration: Long
			): Long =
				value.duration?.inWholeNanoseconds ?: _defaultDuration.inWholeNanoseconds

			override fun expireAfterRead(key: K, value: CacheValue<V>, currentTime: Long, currentDuration: Long): Long =
				currentDuration

		})
		.removalListener(RemovalListener<K, CacheValue<V>> { key, _, cause ->
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
					logger.warn("Persisted Cache $cacheId with limit of $cacheSize overflowed")
				}
			}
		})
		.buildAsync<K, CacheValue<V>>()


	init {
		scope.launch {
			persistence.deleteExpiredByCache(cacheId)                // ensure all expired entries have been deleted
			persistence.findByCache(cacheId).map { entity ->        // restore cache from persistance
				keySerializer.deserialize(entity.key) to entity.toCacheValue()
			}.collect { (key, value) ->
				cache.asMap().compute(key) { _, existingValue ->
					// only over-write if mapping is absent
					return@compute existingValue ?: CompletableFuture.completedFuture(value)
				}
			}
		}
	}

	override suspend fun put(k: K, v: V, duration: Duration?) {
		val future = CompletableFuture<CacheValue<V>>()
		// insert incomplete future early to avoid race conditions
		cache.put(k, future)
		persistence.upsert(createEntity(k, v, duration))
		future.complete(CacheValue(v, duration))
	}

	override suspend fun putIfAbsent(k: K, v: V, duration: Duration?) {
		if (cache.getIfPresent(k)!= null) return
		else put(k, v, duration)
	}

	override suspend fun get(k: K): V? = cache.getIfPresent(k)?.await()?.value


	override suspend fun remove(k: K) {
		persistence.deleteById(keySerializer.serialize(k), cacheId)
		cache.asMap().remove(k)
	}

	private suspend fun createEntity(key: K, value: V, duration: Duration?): KvCacheEntity {
		val (key, value) = withContext(Dispatchers.Default){
			val key = keySerializer.serialize(key)
			val value = valueSerializer.serialize(value)
			key to value
		}
		return KvCacheEntity(
			key = key,
			value = value,
			cache = cacheId,
			expireAt = OffsetDateTime.now().plusSeconds(duration?.inWholeSeconds ?: _defaultDuration.inWholeSeconds),
		)
	}

	private fun KvCacheEntity.toCacheValue() : CacheValue<V> {
		val expireAtSeconds = expireAt.toEpochSecond()
		val secondsNow = Clock.System.now().epochSeconds
		val remainingDuration = (expireAtSeconds - secondsNow).seconds
		return CacheValue(valueSerializer.deserialize(value), remainingDuration)
	}

}