package com.example.todoapp.app.invitation.model

import com.example.todoapp.common.model.NewzroomRoleDto
import com.example.todoapp.common.validation.AtLeastOneFieldNotNull
import jakarta.validation.constraints.*
import java.time.Instant

@AtLeastOneFieldNotNull
data class UpdateInvitationRequest(
	val role: NewzroomRoleDto? = null,
	@field:Future
	val expiresAt: Instant? = null,
	@field:Email
	val email: String? = null,
	@field:FutureOrPresent
	val revokedAt: Instant? = null
)
