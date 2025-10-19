package com.example.todoapp.app.invitation.validation

import com.example.todoapp.app.invitation.model.CreateInvitationRequest
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class CreateInvitationRequestValidationTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `valid request passes validation`() {
        val request = CreateInvitationRequest(
			role = NewzroomRole.WRITER,
			expiresAt = OffsetDateTime.now().plusDays(1).toInstant(),
			email = "test@example.com"
		)
        val violations = validator.validate(request)
		Assertions.assertEquals(0, violations.size)
    }

    @Test
    fun `past expiresAt fails validation`() {
        val request = CreateInvitationRequest(
			role = NewzroomRole.WRITER,
			expiresAt = OffsetDateTime.now().minusDays(1).toInstant(),
			email = "test@example.com"
		)
        val violations = validator.validate(request)
		Assertions.assertEquals(1, violations.size)
    }

    @Test
    fun `invalid email fails validation`() {
        val request = CreateInvitationRequest(
			role = NewzroomRole.WRITER,
			expiresAt = OffsetDateTime.now().plusDays(1).toInstant(),
			email = "not-an-email"
		)
        val violations = validator.validate(request)
		Assertions.assertEquals(1, violations.size)
		Assertions.assertEquals("must be a well-formed email address", violations.first().message)
    }
}