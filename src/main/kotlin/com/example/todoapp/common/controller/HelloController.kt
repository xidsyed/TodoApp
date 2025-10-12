package com.example.todoapp.common.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.slf4j.LoggerFactory

@RestController
class HelloController {
	private val logger = LoggerFactory.getLogger(HelloController::class.java)

	@GetMapping("/hello")
	fun hello(@RequestParam(required = false) name: String?): Map<String, String> {
		return mapOf("message" to "Hello ${name?:""}!")
	}
}