package com.example.todoapp.common.exception

import org.springframework.http.HttpStatus


sealed class DomainEx(
	val status: HttpStatus,
	override val message: String,
	open val props: Map<String, Any> = emptyMap(),
	override val cause: Throwable? = null
) : RuntimeException(message, cause)

open class NotFoundEx(
	val entity: String = "Resource",
	val id: String = "",
	override val props: Map<String, Any> = emptyMap(),
	override val cause: Throwable? = null
) : DomainEx(
	status = HttpStatus.NOT_FOUND,
	message = "$entity $id not found",
	props = props,
	cause = cause
)

open class PermissionDeniedEx(
	reason: String = "You do not have permission to perform this action",
	cause: Throwable? = null,
	override val props: Map<String, Any> = emptyMap()
) : DomainEx(
	status = HttpStatus.FORBIDDEN,
	message = reason,
	props = props,
	cause = cause
)

open class UnprocessableEx(
	reason: String = "Request Validation Failed",
	fieldErrors: List<FieldError>? = null,
	cause: Throwable? = null
) : DomainEx(
	status = HttpStatus.UNPROCESSABLE_ENTITY,
	message = reason,
	props = fieldErrors?.let { mapOf("fieldErrors" to it.map { err -> err.toMap() }) } ?: emptyMap(),
	cause = cause
)

open class InvalidRequestEx(
	reason: String = "Invalid Request",
	fieldErrors: List<FieldError>? = null,
	override val cause: Throwable? = null
) : DomainEx(
	status = HttpStatus.BAD_REQUEST,
	message = reason,
	props = fieldErrors?.let { mapOf("fieldErrors" to it.map { err -> err.toMap() }) } ?: emptyMap(),
	cause = cause
)

open class AlreadyExistsEx(
	val entity: String,
	override val props: Map<String, Any> = emptyMap(),
	override val cause: Throwable?
) : DomainEx(
	status = HttpStatus.CONFLICT,
	message = "$entity already exists",
	props = props,
	cause = cause
)

data class FieldError(val field: String, val message: String) {
	fun toMap() = mapOf("field" to field, "message" to message)
}
