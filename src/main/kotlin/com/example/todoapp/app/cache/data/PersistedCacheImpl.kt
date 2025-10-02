package com.example.todoapp.app.cache.data

import com.example.todoapp.app.cache.data.entity.KvCacheEntity
import com.example.todoapp.app.cache.domain.*
import com.example.todoapp.common.serialize.Serializer
import com.github.benmanes.caffeine.cache.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.future.await
import org.gradle.internal.impldep.kotlinx.coroutines.Dispatchers
import org.gradle.internal.impldep.kotlinx.coroutines.flow.forEach
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import java.time.Duration as JavaDuration


class PersistedCacheImpl<K : Any, V : Any>(
	private val persistence: CachePersistenceRepository,
	val cacheId: String,
	private val keySerializer: Serializer<K>,
	private val valueSerializer: Serializer<V>,
	val defaultTTL: Duration = Duration.INFINITE,
	val cacheSize: Long = 10_000L,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PersistedCache<K, V> {

	private val scope = CoroutineScope(dispatcher + SupervisorJob())
	private val logger = LoggerFactory.getLogger(this.javaClass)

	private val cache = Caffeine.newBuilder()
		.expireAfterWrite(JavaDuration.ofNanos(defaultTTL.inWholeNanoseconds))
		.maximumSize(cacheSize)
		.removalListener(RemovalListener<K, V> { key, _, cause ->
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
		.buildAsync<K, V>()


	init {
		scope.launch {
			// ensure all expired entries have been deleted
			persistence.deleteExpiredByCache(cacheId)
			// restore cache from persistance
			persistence.findByCache(cacheId).map {
				keySerializer.deserialize(it.key) to valueSerializer.deserialize(it.value)
			}.forEach { (key, value) ->
				cache.asMap().compute(key) { _, v ->
					// only over-write if mapping is absent
					return@compute v ?: CompletableFuture.completedFuture(value)
				}
			}
		}
	}

	override suspend fun put(k: K, v: V, ttl: Duration?) {
		cache.asMap().compute(k) { key: K, value: CompletableFuture<V>? ->
			val future = CompletableFuture<V>()
			scope.launch {
				try {
					persistence.upsert(createEntity(k, v, ttl))
					future.complete(v)
				} catch (e: Exception) {
					future.completeExceptionally(e)
				}
			}
			return@compute future
		}

	}

	override suspend fun get(k: K): V? = cache.getIfPresent(k)?.await()


	override suspend fun remove(k: K) {
		persistence.deleteById(keySerializer.serialize(k), cacheId)
		cache.asMap().remove(k)
	}

	private fun createEntity(key: K, value: V, ttl: Duration?): KvCacheEntity {
		return KvCacheEntity(
			key = keySerializer.serialize(key),
			value = valueSerializer.serialize(value),
			cache = cacheId,
			expireAt = OffsetDateTime.now().plusNanos(ttl?.inWholeNanoseconds ?: defaultTTL.inWholeNanoseconds),
		)
	}

}