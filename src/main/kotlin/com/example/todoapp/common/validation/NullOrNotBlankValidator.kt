package com.example.todoapp.common.validation

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.*
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [NullOrNotBlankValidator::class])
@Schema(nullable = true, required = false, minLength = 1, pattern = "\\S")
annotation class NullOrNotBlank(
	val message: String = "must be null or not blank",
	val groups: Array<KClass<*>> = [],
	val payload: Array<KClass<out Payload>> = []
)

// src/main/kotlin/com/example/validation/NullOrNotBlankValidator.kt
class NullOrNotBlankValidator : ConstraintValidator<NullOrNotBlank, CharSequence?> {
	override fun isValid(value: CharSequence?, context: ConstraintValidatorContext?): Boolean {
		// null is valid; non-null must have at least one non-whitespace char
		if (value == null) return true
		return value.isNotBlank()
	}
}
