package com.example.todoapp.app.invitations

import com.example.todoapp.common.model.NewzroomRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CreateInvitationRequest (
	@field:NotNull
	val role: NewzroomRole,
	@field:NotNull
	@field:Future
	val expiresAt: Instant,
	@field:NotNull
	@field:Email
	val email: String
)