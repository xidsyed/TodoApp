package com.example.todoapp.app.invitation.model

import com.example.todoapp.app.auth.roles.data.mapper.entity
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.common.validation.AtLeastOneFieldNotNull
import jakarta.validation.constraints.*
import java.time.Instant

@AtLeastOneFieldNotNull
data class PatchInvitationRequest(
	val role: NewzroomRole? = null,
	@field:Future
	val expiresAt: Instant? = null,
	@field:Email
	val email: String? = null,
)


fun InvitationEntity.applyPatch(patch: PatchInvitationRequest) = copy(
	role = patch.role?.entity() ?: role,
	eat = patch.expiresAt ?: eat,
	email = patch.email ?: email
)