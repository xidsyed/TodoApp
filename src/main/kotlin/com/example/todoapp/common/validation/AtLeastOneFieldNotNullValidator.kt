package com.example.todoapp.common.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AtLeastOneFieldNotNullValidator::class])
annotation class AtLeastOneFieldNotNull(
	val message: String = "At least one field must be non-null",
	val groups: Array<KClass<*>> = [],
	val payload: Array<KClass<out Payload>> = []
)

class AtLeastOneFieldNotNullValidator :
	ConstraintValidator<AtLeastOneFieldNotNull, Any> {

	override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
		if (value == null) return false

		return value::class.memberProperties.any { property ->
			// Make the property accessible to handle private classes
			property.isAccessible = true
			property.call(value) != null
		}
	}
}
