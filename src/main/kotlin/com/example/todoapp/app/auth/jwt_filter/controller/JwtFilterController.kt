package com.example.todoapp.app.auth.jwt_filter.controller

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import com.example.todoapp.app.auth.jwt_filter.model.BlacklistedTokenDto
import com.example.todoapp.app.auth.roles.annotations.RequireAdmin
import com.example.todoapp.app.users.*
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
class JwtFilterController(
	private val jwtFilterService: JwtFilterService,
	private val userRepository: UserRepository
) {

	@PostMapping("blacklist")
	suspend fun blacklistUser(
		@RequestParam("sub") sub: String
	): ResponseEntity<BlacklistedTokenDto> {
		val userId = UUID.fromString(sub)
		userRepository.findById(userId) ?: throw userNotFound(userId.toString())
		jwtFilterService.blacklistToken(sub, Instant.now())
		return ok(BlacklistedTokenDto(sub, Instant.now().epochSecond))
	}

	@GetMapping("blacklist")
	suspend fun fetchBlacklist(): Flow<BlacklistedTokenDto> {
		return jwtFilterService.fetchAllTokens()
			.map { (key, value) -> BlacklistedTokenDto(sub = key, blacklistedAt = value.epochSecond) }
	}


}