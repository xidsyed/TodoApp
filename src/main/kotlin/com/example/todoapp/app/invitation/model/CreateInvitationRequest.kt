package com.example.todoapp.app.invitation.model

import com.example.todoapp.common.model.NewzroomRoleDto
import jakarta.validation.constraints.*
import java.time.Instant

data class CreateInvitationRequest (
	@field:NotNull
	val role: NewzroomRoleDto,
	@field:NotNull
	@field:Future
	val expiresAt: Instant,
	@field:NotNull
	@field:Email
	val email: String
)