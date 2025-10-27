package com.example.todoapp.app.users.model

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import java.time.Instant
import java.util.*

data class UserInternalDto(
	val id: UUID,
	val email: String,
	val createdAt: Instant? = null,
	val freezeTill: Instant? = null,
	val freezeCause: String? = null,
	val name: String,
	val picture: String? = null,
	val role: NewzroomRoleEntity,
)

