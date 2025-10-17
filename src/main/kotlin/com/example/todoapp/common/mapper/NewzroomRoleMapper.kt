package com.example.todoapp.common.mapper

import com.example.todoapp.common.data.entity.NewzroomRoleEntity
import com.example.todoapp.common.model.NewzroomRoleDto

fun NewzroomRoleDto.entity(): NewzroomRoleEntity = when (this) {
    NewzroomRoleDto.WRITER -> NewzroomRoleEntity.WRITER
    NewzroomRoleDto.ADMIN -> NewzroomRoleEntity.ADMIN
}

fun NewzroomRoleEntity.dto(): NewzroomRoleDto = when (this) {
    NewzroomRoleEntity.WRITER -> NewzroomRoleDto.WRITER
    NewzroomRoleEntity.ADMIN -> NewzroomRoleDto.ADMIN
}