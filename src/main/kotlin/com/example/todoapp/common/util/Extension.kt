package com.example.todoapp.common.util

import org.slf4j.*
import org.springframework.http.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.ErrorResponseException
import java.util.*

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(this::class.java)


fun err(status: HttpStatus, detail: String? = "", cause: Exception? = null) =
	ErrorResponseException(status, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail), cause)

fun <T : Any> res(status: HttpStatus, body: T? = null, headers: HttpHeaders? = null): ResponseEntity<T> {
	val builder = ResponseEntity.status(status)
	builder.contentType(
		when (body) {
			null, is String -> MediaType.TEXT_PLAIN
			else -> MediaType.APPLICATION_JSON
		}
	)
	headers?.let { builder.headers(it) }
	return if (body != null) builder.body(body) else builder.build()
}

fun internalErr(cause: String = "Internal Error Occurred", e: Exception? = null) =
	err(HttpStatus.INTERNAL_SERVER_ERROR, cause, e)

fun Jwt.sub() : UUID = UUID.fromString(this.subject)