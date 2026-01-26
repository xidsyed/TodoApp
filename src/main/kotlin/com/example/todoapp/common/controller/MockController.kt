package com.example.todoapp.common.controller

import com.example.todoapp.common.util.err
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.*
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@EnableReactiveMethodSecurity
@RequestMapping("/mock")
@Validated
class MockController {
	data class MockResponse(
		val message: String, val code: Int, val extra: Map<String, Any>
	)

	data class BadJson(
		val foo: String,
		val bar: String
	)

	@GetMapping("/{id}")
	suspend fun getMockResponse(
		@PathVariable id: Int
	): MockResponse {
		val status = HttpStatus.valueOf(id)
		if (id !in 200..<300) {
			throw err(
				status, "Failed!", RuntimeException("Mock Runtime Exception"), props = mapOf(
					"time" to Instant.now(),
					"type" to "newzer_error"
				)
			)
		}

		return MockResponse(
			"Mock Success!",
			id,
			mapOf(
				"time" to Instant.now(),
				"type" to "newzer_response",
				"response" to mapOf(
					"name" to "john",
					"id" to UUID.randomUUID().toString()
				)
			)
		)
	}

	@GetMapping("/badjson")
	@ApiResponse(
		responseCode = "200",
		content = [Content(schema = Schema(implementation = BadJson::class))]
	)
	suspend fun getBadJsonResponse(): ResponseEntity<Any> {
		return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(
			"""
				{
					"foo" : "bar",
					"bar : "baz"
				}
				""".trimIndent()
		)
	}
}