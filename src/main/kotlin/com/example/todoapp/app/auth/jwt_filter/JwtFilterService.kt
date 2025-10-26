package com.example.todoapp.app.auth.jwt_filter

import com.example.todoapp.core.cache.CachePersistenceRepository
import com.example.todoapp.core.cache.data.PersistedCacheImpl
import com.example.todoapp.core.serializer.createJacksonSerializer
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.time.*


@Component
class JwtFilterService(
	cachePersistenceRepository: CachePersistenceRepository,
	jsonMapper: JsonMapper,
	jwtFilterProperties: JwtFilterProperties
) {
	private val cache = PersistedCacheImpl<String, Instant>(
		persistence = cachePersistenceRepository,
		cacheId = "jwt_filter",
		keySerializer = createJacksonSerializer<String>(jsonMapper),
		valueSerializer = createJacksonSerializer<Instant>(jsonMapper),
		defaultDuration = Duration.ofSeconds(jwtFilterProperties.jwtExpirationDurationInSeconds),
		cacheSize = 1_000_000,
		dispatcher = Dispatchers.IO
	)

	suspend fun isTokenValid(sub: String, iat: Instant): Boolean {
		val minIat = cache.get(sub)
		val tokenNotBlacklisted = minIat == null
		val tokenIssuedAfterMinIat = !tokenNotBlacklisted && iat > minIat
		return !(tokenNotBlacklisted || tokenIssuedAfterMinIat)
	}

	suspend fun blacklistToken(sub: String, minIat: Instant = Instant.now(), duration: Duration? = null) {
		cache.put(sub, minIat, duration)
	}

	suspend fun fetchAllTokens() = cache.getAll()



}