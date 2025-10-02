package com.example.todoapp.config

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object TestUtils {
	fun testLog(obj: Any?) {
		val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).time.toString().substringBefore(".")
		println("$now :: $obj")
	}
}


/*private suspend fun longTask(): Boolean {
	log("loader :: verifying user... ")
	delay(5000)
	log("loader :: user verified")
	return true
}

@Test
fun `test cache with long running future`() = runBlocking {
	log("START TEST")

	val cache = cacheManager.getCache(USER_BLACKLIST) ?: throw IllegalArgumentException("Cache not found")
	val user = "user1"

	log("test :: performing cache-put")
	cache.put(user, future { longTask() })
	log("test :: cache-put complete")

	log("test :: performing retrieve")
	val retrieved = cache.retrieve(user)
	log("test :: retrieving complete :: $retrieved")	// java.util.concurrent.CompletableFuture@7f31937b[Completed normally]

	log("test :: performing get")
	val getTyped = cache.get<CompletableFuture<Boolean>>(user)
	log("test :: get complete :: $getTyped")			// java.util.concurrent.CompletableFuture@2f4fc18[Not completed, 1 dependents]

	log("test :: performing retrieve-await")
	val retrievedAwaited = retrieved?.await()
	log("test :: retrieve-await complete :: $retrievedAwaited") // ValueWrapper for [java.util.concurrent.CompletableFuture@2f4fc18[Not completed, 1 dependents]]

	log("test :: performing get-await")
	val getTypedAwaited = getTyped?.await()
	log("test :: get-await complete :: $getTypedAwaited") // true

	log("END TEST")
}


@Test
fun `testing cache with future`() = runBlocking {
	log("START TEST")
	val cache = cacheManager.getCache(USER_BLACKLIST) ?: throw IllegalArgumentException("Cache not found")
	val user = "user1"
	log("test :: performing cache-put")
	cache.put(user, future { longTask() })
	log("test :: cache-put complete")

	log("test :: performing retrieve")
	val retrieved = cache.retrieve(user)
	log("test :: retrieving complete :: $retrieved")	// java.util.concurrent.CompletableFuture@7f31937b[Completed normally]
	log("test :: performing get")
	val getTyped = cache.get<CompletableFuture<Boolean>>(user)
	log("test :: get complete :: $getTyped")			// java.util.concurrent.CompletableFuture@2f4fc18[Not completed, 1 dependents]

	log("test :: performing retrieve-await")
	val retrievedAwaited = retrieved?.await()
	log("test :: retrieve-await complete :: $retrievedAwaited") // ValueWrapper for [java.util.concurrent.CompletableFuture@2f4fc18[Not completed, 1 dependents]]
	log("test :: performing get-await")
	val getTypedAwaited = getTyped?.await()
	log("test :: get-await complete :: $getTypedAwaited") // true

	log("END TEST")
}

@Test
fun `testing cache without future`() = runBlocking {
	log("START TEST")
	val cache = cacheManager.getCache(USER_BLACKLIST) ?: throw IllegalArgumentException("Cache not found")
	val user = "user1"
	log("test :: performing cache-put")
	cache.put(user, true)
	log("test :: cache-put complete")

	log("test :: performing retrieve")
	val retrieved = cache.retrieve(user)
	log("test :: retrieving complete :: $retrieved")	// java.util.concurrent.CompletableFuture@7f31937b[Completed normally]

	log("test :: performing get")
	val getTyped = cache.get<Boolean>(user)
	log("test :: get complete :: $getTyped")			// java.util.concurrent.CompletableFuture@2f4fc18[Not completed, 1 dependents]

	log("test :: performing retrieve-await")
	val retrievedAwaited = retrieved?.await() as? Boolean
	log("test :: retrieve-await complete :: $retrievedAwaited") // ValueWrapper for [java.util.concurrent.CompletableFuture@2f4fc18[Not completed, 1 dependents]]
	//log("test :: performing get-await")
	//val getTypedAwaited = getTyped?.await()
	//log("test :: get-await complete :: $getTypedAwaited") // true

	log("END TEST")
}*/
