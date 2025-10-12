package com.example.todoapp.app.invitations

import com.example.todoapp.common.model.NewzroomRole
import jakarta.validation.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class CreateInvitationRequestTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `valid request passes validation`() {
        val request = CreateInvitationRequest(
            role = NewzroomRole.WRITER,
            expiresAt = OffsetDateTime.now().plusDays(1).toInstant() ,
            email = "test@example.com"
        )
        val violations = validator.validate(request)
        assertEquals(0, violations.size)
    }

    @Test
    fun `past expiresAt fails validation`() {
        val request = CreateInvitationRequest(
            role = NewzroomRole.WRITER,
            expiresAt = OffsetDateTime.now().minusDays(1).toInstant(),
            email = "test@example.com"
        )
        val violations = validator.validate(request)
        assertEquals(1, violations.size)
    }

    @Test
    fun `invalid email fails validation`() {
        val request = CreateInvitationRequest(
            role = NewzroomRole.WRITER,
            expiresAt = OffsetDateTime.now().plusDays(1).toInstant(),
            email = "not-an-email"
        )
        val violations = validator.validate(request)
        assertEquals(1, violations.size)
        assertEquals("must be a well-formed email address", violations.first().message)
    }
}