package com.example.todoapp.app.users.mapper

import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.app.users.model.UserDto
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole

fun UserEntity.dto(): UserDto {
    return UserDto(
        id = this.userId,
        name = this.displayName,
        picture = this.profilePic,
        role = NewzroomRole.valueOf(this.role.name.uppercase())
    )
}
