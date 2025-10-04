package com.example.todoapp.cache.data

import com.example.todoapp.app.cache.data.PersistedCacheImpl
import com.example.todoapp.app.cache.domain.CachePersistenceRepository
import com.example.todoapp.common.serialize.jacksonSerializer
import com.example.todoapp.config.TestUtils.testLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
class PersistedCacheImplIntegrationTest @Autowired constructor(
	private val persistence: CachePersistenceRepository,
	private val dbClient: DatabaseClient
) {

	// --- Helpers / constants ---
	private data class MyTestValue(val message: String, val code: Int)

	private fun stringKeySerializer() = jacksonSerializer<String>()
	private fun myValueSerializer() = jacksonSerializer<MyTestValue>()

	private fun makeCache(
		cacheId: String,
		defaultTTLSeconds: Long? = null
	) = PersistedCacheImpl(persistence, cacheId, stringKeySerializer(), myValueSerializer(), defaultTTLSeconds?.seconds)


	/**
	 * Polls the provided condition until it becomes true or timeout elapses.
	 * Uses a small poll interval so tests typically complete faster than fixed delays.
	 */
	private suspend fun awaitUntil(
		timeoutMillis: Long = 2000,
		pollIntervalMillis: Long = 100,
		condition: suspend () -> Boolean
	) {
		withTimeout(timeoutMillis) {
			delay(pollIntervalMillis)
			while (!condition()) {
				delay(pollIntervalMillis)
			}
		}
	}

	@AfterEach
	fun cleanup(): Unit = runBlocking {
		// Ensure DB table is cleaned between tests
		dbClient.sql("TRUNCATE TABLE cache_kv").then().block()
	}

	// ------------------------
	// In-memory behavior tests
	// -------------------------

	@Test
	fun `put overwrites existing entry in-memory`() = runBlocking {
		val cache = makeCache("overwrite-cache")
		val key = "overwrite-key"
		val value1 = MyTestValue("value1", 1)
		val value2 = MyTestValue("value2", 2)

		cache.put(key, value1)
		assertEquals(value1, cache.get(key), "initial put should return stored value")

		cache.put(key, value2)
		assertEquals(value2, cache.get(key), "subsequent put should overwrite previous value")
	}

	@Test
	fun `putIfAbsent does not overwrite existing entry`() = runBlocking {
		val cache = makeCache("put-if-absent-cache")
		val key = "put-if-absent-key"
		val original = MyTestValue("value1", 1)
		val newAttempt = MyTestValue("value2", 2)

		cache.put(key, original)
		assertEquals(original, cache.get(key))

		cache.putIfAbsent(key, newAttempt)
		assertEquals(original, cache.get(key), "putIfAbsent must not overwrite existing mapping")
	}

	@Test
	fun `remove evicts only the targeted key`(): Unit = runBlocking {
		val cache = makeCache("remove-cache")
		val toRemove = "remove-key"
		val toKeep = "keep-key"
		val vRemove = MyTestValue("to be removed", 789)
		val vKeep = MyTestValue("to be kept", 123)

		cache.put(toRemove, vRemove)
		cache.put(toKeep, vKeep)

		assertNotNull(cache.get(toRemove))
		assertNotNull(cache.get(toKeep))

		cache.remove(toRemove)

		assertNull(cache.get(toRemove), "removed key should no longer be present")
		assertNotNull(cache.get(toKeep), "removing one key must not affect other keys")
	}

	@Test
	fun `default TTL is honored for in-memory entries`() = runBlocking {
		val defaultTtlSeconds = 2L
		val cache = makeCache("default-ttl-in-memory-cache", defaultTTLSeconds = defaultTtlSeconds)

		val freshKey = "fresh-key"
		val expiredKey = "expired-key"
		val value = MyTestValue("ttl test", 1)

		// Fresh: inserted and checked before TTL expiry
		cache.put(freshKey, value)
		// Wait up to 1 second for the item to be present (should be immediate)
		awaitUntil(timeoutMillis = 1000) { cache.get(freshKey) != null }
		assertNotNull(cache.get(freshKey), "should not expire before TTL")

		// Expired: inserted then wait until TTL passes
		cache.put(expiredKey, value)
		// Wait a bit longer than TTL to ensure expiry; using polling to avoid fixed long sleeps
		awaitUntil(timeoutMillis = (defaultTtlSeconds * 1000) + 1500) { cache.get(expiredKey) == null }
		assertNull(cache.get(expiredKey), "should expire after default TTL")
	}

	@Test
	fun `entry-specific TTL overrides default TTL for in-memory entries`(): Unit = runBlocking {
		val cache = makeCache("entry-ttl-in-memory-cache", defaultTTLSeconds = 10L) // long default

		val shortLivedKey = "short-lived-key"
		val longLivedKey = "long-lived-key"
		val value = MyTestValue("entry ttl", 2)

		// short-lived: TTL = 2s
		cache.put(shortLivedKey, value, 2.seconds)
		awaitUntil(timeoutMillis = 1000) { cache.get(shortLivedKey) != null } // must exist initially
		assertNotNull(cache.get(shortLivedKey))

		// Wait for expiry (2s + small buffer)
		awaitUntil(timeoutMillis = 3500) { cache.get(shortLivedKey) == null }
		assertNull(cache.get(shortLivedKey), "entry TTL should cause expiry")

		// long-lived: TTL explicitly long so it should still be present
		cache.put(longLivedKey, value, 10.seconds)
		assertNotNull(cache.get(longLivedKey))
	}

	// ------------------------
	// Persistence behavior tests
	// -------------------------

	@Test
	fun `persisted entries survive across cache instances`() = runBlocking {
		val cacheId = "persist-cache"
		val cache1 = makeCache(cacheId)

		val key = "persist-key"
		val value = MyTestValue("it's persisted", 456)

		cache1.put(key, value)
		assertNotNull(cache1.get(key))

		// Wait until persistence layer has the entry (polling for determinism)
		// read-back using a fresh instance of the same cache persistence to ensure persistence works
		val tmpCache = makeCache(cacheId)
		awaitUntil {
			testLog(tmpCache.get(key))
			tmpCache.get(key) != null
		}


		// Create a new cache instance which should load persisted entries
		val cache2 = makeCache(cacheId)
		// wait until new instance initializes and sees the value
		awaitUntil(timeoutMillis = 2000) { cache2.get(key) != null }

		val retrieved = cache2.get(key)
		assertNotNull(retrieved)
		assertEquals(value, retrieved)
	}

	@Test
	fun `removed entries are not restored by new cache instances`(): Unit = runBlocking {
		val cacheId = "deleted-restore-cache"
		val cache1 = makeCache(cacheId)

		val deletedKey = "deleted-key"
		val keptKey = "kept-key"
		val value = MyTestValue("should be gone after delete", 4)

		cache1.put(deletedKey, value)
		cache1.put(keptKey, value)

		// Verify present then remove one
		assertNotNull(cache1.get(deletedKey))
		assertNotNull(cache1.get(keptKey))
		cache1.remove(deletedKey)
		assertNull(cache1.get(deletedKey))

		// Wait for persistence cleanup to be reflected
		val tmp = makeCache(cacheId)
		awaitUntil(timeoutMillis = 2000) {
			tmp.get(deletedKey) == null && tmp.get(keptKey) != null
		}
	}

	@Test
	fun `expired entries are not restored by new cache instances`(): Unit = runBlocking {
		val cacheId = "expired-restore-cache"
		val defaultTtlSeconds = 2L
		val cache1 = makeCache(cacheId, defaultTTLSeconds = defaultTtlSeconds)

		val defaultExpired = "default-ttl-expired"
		val entryExpired = "entry-ttl-expired"
		val freshKey = "fresh-key"
		val value = MyTestValue("will expire", 1)

		cache1.put(defaultExpired, value) // uses default TTL
		cache1.put(entryExpired, value, 2.seconds) // explicit entry TTL
		cache1.put(freshKey, value, 10.seconds) // should remain fresh

		// Wait until in-memory expirations occur
		awaitUntil(timeoutMillis = (defaultTtlSeconds * 1000) + 1500) {
			cache1.get(defaultExpired) == null && cache1.get(entryExpired) == null
		}
		assertNull(cache1.get(defaultExpired))
		assertNull(cache1.get(entryExpired))
		assertNotNull(cache1.get(freshKey))

		// Wait for persistence cleanup to propagate, then create new instance
		awaitUntil(timeoutMillis = 2000) {
			val tmp = makeCache(cacheId)
			tmp.get(defaultExpired) == null && tmp.get(entryExpired) == null
		}

		val cache2 = makeCache(cacheId, defaultTTLSeconds = defaultTtlSeconds)
		awaitUntil(timeoutMillis = 2000) { cache2.get(freshKey) != null }

		assertNull(cache2.get(defaultExpired), "expired entry (default TTL) must not be restored")
		assertNull(cache2.get(entryExpired), "expired entry (entry TTL) must not be restored")
		assertNotNull(cache2.get(freshKey), "fresh entry must be restored")
	}

	@Test
	@Disabled("This test is purely for obtaining a rough approximation of the cache's performance ")
	fun `cache performance`() = runBlocking {
		val cache = makeCache("performance-cache")
		val durationSeconds = 3
		val durationMillis = durationSeconds * 1000L
		val numRuns = 3

		// thread-safe collection for concurrent producers
		val writtenKeys = ConcurrentLinkedQueue<String>()

		// bound concurrency (logical)
		val concurrency = 500
		val sem = Semaphore(concurrency)

		// --- Write performance ---
		testLog("--- Write Performance (duration: $durationSeconds seconds per run) ---")
		repeat(numRuns) { run ->
			val completedOps = LongAdder()
			var index = 0
			delay(1_000) //
			testLog("Write Run Started (#${run + 1})")

			// ensure launched children are cancelled when timeout fires
			withTimeoutOrNull(durationMillis) {
				coroutineScope {
					while (isActive) {
						val key = "run-${run + 1}-write-$index"
						val value = MyTestValue("some value", index)

						// backpressure: suspends if concurrency limit reached
						sem.acquire()

						// child coroutines are part of the coroutineScope and will be cancelled on timeout
						launch(Dispatchers.IO) {
							try {
								// perform the (suspending) put
								cache.put(key, value)
								writtenKeys.add(key)
								completedOps.increment()
							}finally {
								sem.release()
							}
						}

						index++ // increment in producer loop (avoid racing with child)
					}
				}
			}

			val ops = completedOps.sum().toInt()
			val avgTimeMs = if (ops > 0) durationMillis.toDouble() / ops else 0.0
			testLog("Write Run #${run + 1}: $ops operations, avg: ${"%.4f".format(avgTimeMs)} ms/op, rps: ${ops / durationSeconds}")
		}

		testLog("10 Second Timeout to reset CPU temperature...")
		delay(10_000)

		// --- Read performance ---
		testLog("--- Read Performance (duration: $durationSeconds seconds per run) ---")
		if (writtenKeys.isEmpty()) {
			testLog("No keys were written, skipping read performance test.")
			return@runBlocking
		}

		repeat(numRuns) { run ->
			val completedOps = LongAdder()
			var index = 0
			val keysSnapshot = writtenKeys.toList() // stable snapshot for this run
			delay(1_000)
			testLog("Read Run Started (#${run + 1})")

			withTimeoutOrNull(durationMillis) {
				coroutineScope {
					while (isActive) {
						val keyToRead = keysSnapshot[index % keysSnapshot.size]

						sem.acquire()
						launch(Dispatchers.IO) {
							try {
								cache.get(keyToRead)
								completedOps.increment()
							} finally {
								sem.release()
							}
						}

						index++
					}
				}
			}

			val ops = completedOps.sum().toInt()
			val avgTimeMs = if (ops > 0) durationMillis.toDouble() / ops else 0.0
			testLog("Read Run #${run + 1}: $ops operations, avg: ${"%.4f".format(avgTimeMs)} ms/op, rps: ${ops / durationSeconds}")
		}
	}

}
