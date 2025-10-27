package com.example.todoapp.common.exception

import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

	@ExceptionHandler(DomainEx::class)
	fun domainExceptionHandler(ex: DomainEx): ErrorResponseException {
		return ErrorResponseException(
			ex.status,
			ProblemDetail.forStatusAndDetail(ex.status, ex.message).apply { properties = ex.props },
			ex.cause
		)
	}

}