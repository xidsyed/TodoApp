package com.example.todoapp.app.invitation.model

import com.example.todoapp.app.users.model.UserDto
import com.example.todoapp.common.model.NewzroomRoleDto
import java.time.Instant
import java.util.*

data class InvitationDto(
	var id: UUID? = null,
	val email: String,
	val createdBy: UserDto,
	val role: NewzroomRoleDto,
	val eat: Instant,
	val revokedAt: Instant? = null,
	val assignedTo: UserDto? = null,
	val createdAt: Instant,
)