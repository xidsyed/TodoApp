package com.example.todoapp.common.validation

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

@AtLeastOneFieldNotNull
private data class TestAtLeastOneFieldNotNull(
	val role: String? = null,
	val expiresAt: String? = null,
	val email: String? = null,
	val revokedAt: String? = null
)

class AtLeastOneFieldNotNullValidatorTest {

	private val validator = Validation.buildDefaultValidatorFactory().validator

	@Test
	fun `should fail when all fields are null`() {
		val request = TestAtLeastOneFieldNotNull()

		val violations = validator.validate(request)

		assertEquals(1, violations.size, "Expected one validation violation")
		assertTrue(
			violations.first().message.contains("At least one field must be non-null"),
			"Expected message to mention non-null requirement"
		)
	}

	@Test
	fun `should pass when one field is non-null`() {
		val request = TestAtLeastOneFieldNotNull(email = "test@example.com")

		val violations = validator.validate(request)

		assertTrue(violations.isEmpty(), "Expected no validation violations. Instead found: $violations")
	}

	@Test
	fun `should pass when multiple fields are non-null`() {
		val request = TestAtLeastOneFieldNotNull(
			role = "writer",
			expiresAt = "2025-12-31T00:00:00Z"
		)

		val violations = validator.validate(request)

		assertTrue(violations.isEmpty(), "Expected no validation violations. Instead found: $violations")
	}

	@Test
	fun `should fail when all fields are null and provide custom message`() {
		val request = TestAtLeastOneFieldNotNull()
		val violations = validator.validate(request)

		assertEquals(1, violations.size)
		assertEquals("At least one field must be non-null", violations.first().message)
	}
}
