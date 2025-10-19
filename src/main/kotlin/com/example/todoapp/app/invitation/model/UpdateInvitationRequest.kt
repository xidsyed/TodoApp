package com.example.todoapp.app.invitation.model

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.common.validation.AtLeastOneFieldNotNull
import jakarta.validation.constraints.*
import java.time.Instant

@AtLeastOneFieldNotNull
data class UpdateInvitationRequest(
	val role: NewzroomRole? = null,
	@field:Future
	val expiresAt: Instant? = null,
	@field:Email
	val email: String? = null,
	@field:FutureOrPresent
	val revokedAt: Instant? = null
)
