package com.example.todoapp.app.invitation.model

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.app.users.model.UserDto
import java.time.Instant
import java.util.*

data class InvitationDto(
	val id: UUID,
	val email: String,
	val assignor: UserDto,
	val role: NewzroomRole,
	val eat: Instant,
	val assignee: UserDto? = null,
	val createdAt: Instant,
)