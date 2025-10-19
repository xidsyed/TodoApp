package com.example.todoapp.app.invitation.model

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import jakarta.validation.constraints.*
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