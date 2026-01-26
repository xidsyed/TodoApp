package com.example.todoapp.common.controller

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.web.bind.annotation.*

@RestController
@EnableReactiveMethodSecurity
@RequestMapping("/hello")
class HelloController(
	private val jwtFilterService: JwtFilterService
) {
	private val logger = LoggerFactory.getLogger(HelloController::class.java)

	@GetMapping("")
	suspend fun hello(
		@RequestParam name: String? = "Anon",
	): ResponseEntity<Map<String, String>> {
		return ResponseEntity.status(HttpStatus.OK).body(mapOf("message" to "Hello ${name ?: ""}!"))
	}


}