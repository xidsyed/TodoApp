package com.example.todoapp.app.users.model

import com.example.todoapp.common.model.NewzroomRoleDto
import java.util.UUID

data class UserDto(
    val id: UUID,
    val name: String,
    val picture: String?,
    val role: NewzroomRoleDto
)
