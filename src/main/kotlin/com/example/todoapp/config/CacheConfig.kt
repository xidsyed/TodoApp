package com.example.todoapp.config

import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.cache.annotation.EnableCaching
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import org.springframework.cache.Cache
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.coroutineContext

@Configuration
@EnableCaching
class CacheConfig {

	companion object {
		const val DB_ROW_CACHE = "dbRowCache"
		const val USER_BLACKLIST = "userBlacklist"
	}




	@Bean
	fun caffeineCacheManager(): CacheManager {
		val manager = CaffeineCacheManager()
		manager.setAsyncCacheMode(true)

		val cache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(5))
			.maximumSize(10_000)
			.buildAsync<String, Any>()


		manager.registerCustomCache(
			DB_ROW_CACHE,
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(5))
				.maximumSize(10_000)
				.buildAsync()
		)

		manager.registerCustomCache(
			USER_BLACKLIST,
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(10))
				.maximumSize(100_000)
				.buildAsync()
		)

		return manager
	}



}
