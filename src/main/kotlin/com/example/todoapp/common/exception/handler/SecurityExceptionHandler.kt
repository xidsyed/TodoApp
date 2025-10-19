package com.example.todoapp.common.exception.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class SecurityExceptionHandler {
	@ExceptionHandler(AccessDeniedException::class)
	fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<Map<String, String>> {
		val body = mapOf(
			"status" to HttpStatus.FORBIDDEN.reasonPhrase,
			"message" to "You do not have permission to access this resource."
		)
		return ResponseEntity(body, HttpStatus.FORBIDDEN)
	}
}