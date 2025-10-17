package com.example.todoapp.common.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlinx.datetime.Instant
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@jakarta.validation.Constraint(validatedBy = [FutureKtxInstantValidator::class])
annotation class FutureKtxInstant (
	val message: String = "must be in the future",
	val groups: Array<KClass<*>> = [],
	val payload: Array<KClass<out Payload>> = []
)

class FutureKtxInstantValidator(): ConstraintValidator<FutureKtxInstant, Instant> {
	override fun isValid(
		value: Instant?,
		context: ConstraintValidatorContext?
	): Boolean {
		if (value == null) return true // use @NotNull if you disallow nulls
		return value > kotlinx.datetime.Clock.System.now()
	}

}

