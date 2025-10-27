package com.example.todoapp.app.users.mapper

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.app.users.model.*

fun UserEntity.dto(): UserDto {
	return UserDto(
		id = this.userId,
		name = this.displayName,
		picture = this.profilePic,
		role = NewzroomRole.valueOf(this.role.name.uppercase())
	)
}

fun UserEntity.internalDto(): UserInternalDto = UserInternalDto(
	id = userId,
	email = email,
	createdAt = createdAt,
	freezeTill = freezeTill,
	freezeCause = freezeCause,
	name = displayName,
	picture = profilePic,
	role = role
)