package com.example.todoapp.common.exception.handler

import com.example.todoapp.common.exception.CommonNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CommonControllerAdvice {
		@ExceptionHandler(CommonNotFoundException::class)
		fun handleCommonNotFoundException(ex: CommonNotFoundException) : ResponseEntity<Any> {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))
		}
}