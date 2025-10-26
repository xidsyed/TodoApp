package com.example.todoapp.common.controller

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.*

@RestController
@EnableReactiveMethodSecurity
@RequestMapping("/hello")
class HelloController(
	private val jwtFilterService: JwtFilterService
) {
	private val logger = LoggerFactory.getLogger(HelloController::class.java)

	@GetMapping("")
	suspend fun hello(
		@RequestParam(required = false) name: String?,
	): ResponseEntity<Map<String, String>> {
		return ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "Hello ${name ?: ""}!"))
	}

	@GetMapping("blacklist_me")
	suspend fun blacklistMe(
		@AuthenticationPrincipal jwt: Jwt
	): ResponseEntity<Map<String, String>> {
		val userId = jwt.subject
		val blacklistDuration = Duration.ofSeconds(30)
		jwtFilterService.blacklistToken(userId, Instant.now(), blacklistDuration)
		return ok(mapOf("message" to "blacklisted for $blacklistDuration"))
	}
}