package com.example.todoapp.app.auth.jwt_filter.controller

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import com.example.todoapp.app.auth.jwt_filter.model.BlacklistedTokenDto
import com.example.todoapp.app.auth.roles.annotations.RequireAdmin
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.exception.UserNotFoundEx
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.*
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RequireAdmin
@RestController
@EnableReactiveMethodSecurity
@RequestMapping("/jwt_filter")
@Tag(name = "jwt filter", description = "Allows authorized clients to blacklist user jwt based on `iat`")
class JwtFilterController(
	private val jwtFilterService: JwtFilterService,
	private val userRepository: UserRepository
) {

	@PostMapping("blacklist")
	suspend fun blacklistUser(
		@Parameter(name = "sub", description = "the subject's uuid to be blacklisted")
		@RequestParam("sub") sub: String
	): ResponseEntity<BlacklistedTokenDto> {
		val userId = UUID.fromString(sub)
		userRepository.findById(userId) ?: throw UserNotFoundEx(userId)
		jwtFilterService.blacklistOldTokensForSub(sub, Instant.now())
		return ok(BlacklistedTokenDto(sub, Instant.now().epochSecond))
	}

	@GetMapping("blacklist")
	suspend fun fetchBlacklist(): Flow<BlacklistedTokenDto> {
		return jwtFilterService.fetchAllTokens()
			.map { (key, value) -> BlacklistedTokenDto(sub = key, blacklistedAt = value.epochSecond) }
	}


}