package com.example.todoapp.app.users.mapper

import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.app.users.model.UserDto
import com.example.todoapp.common.model.NewzroomRoleDto

fun UserEntity.dto(): UserDto {
    return UserDto(
        id = this.userId,
        name = this.displayName,
        picture = this.profilePic,
        role = NewzroomRoleDto.valueOf(this.role.name.uppercase())
    )
}
