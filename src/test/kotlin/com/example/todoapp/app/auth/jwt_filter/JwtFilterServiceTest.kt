package com.example.todoapp.app.auth.jwt_filter

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
class JwtFilterServiceTest {

    @Autowired
    private lateinit var jwtFilterService: JwtFilterService

    @Test
    fun `isBlacklisted should return false for a token that is not blacklisted`() = runBlocking {
        val sub = "user1"
        val iat = Instant.now()

        val isBlacklisted = jwtFilterService.isTokenValid(sub, iat)

        assertFalse(isBlacklisted)
    }

    @Test
    fun `isBlacklisted should return true for a token that is blacklisted`() = runBlocking {
        val sub = "user2"
        val iat = Instant.now()

        jwtFilterService.blacklistToken(sub, iat)
        val isBlacklisted = jwtFilterService.isTokenValid(sub, iat)

        assertTrue(isBlacklisted)
    }

    @Test
    fun `isBlacklisted should return false for a token issued after the minIat`() = runBlocking {
        val sub = "user3"
        val minIat = Instant.now()
        val newTokenIat = minIat.plus(1, ChronoUnit.SECONDS)

        jwtFilterService.blacklistToken(sub, minIat)
        val isBlacklisted = jwtFilterService.isTokenValid(sub, newTokenIat)

        assertFalse(isBlacklisted)
    }

    @Test
    fun `isBlacklisted should return true for a token issued before the minIat`() = runBlocking {
        val sub = "user4"
        val minIat = Instant.now()
        val oldTokenIat = minIat.minus(1, ChronoUnit.SECONDS)

        jwtFilterService.blacklistToken(sub, minIat)
        val isBlacklisted = jwtFilterService.isTokenValid(sub, oldTokenIat)

        assertTrue(isBlacklisted)
    }
}