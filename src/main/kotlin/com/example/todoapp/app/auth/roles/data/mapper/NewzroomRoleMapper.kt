package com.example.todoapp.app.auth.roles.data.mapper

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import org.slf4j.LoggerFactory
import kotlin.jvm.java

private val logger = LoggerFactory.getLogger(NewzroomRole::class.java)

fun NewzroomRole.entity(): NewzroomRoleEntity = when (this) {
    NewzroomRole.WRITER -> NewzroomRoleEntity.WRITER
    NewzroomRole.ADMIN -> NewzroomRoleEntity.ADMIN
	NewzroomRole.NONE -> {
		logger.error("Attempted to convert NONE to NewzroomRoleEntity. NONE is not a valid role.")
		throw IllegalArgumentException("NONE is not a valid role")
	}
}

fun NewzroomRoleEntity.dto(): NewzroomRole = when (this) {
    NewzroomRoleEntity.WRITER -> NewzroomRole.WRITER
    NewzroomRoleEntity.ADMIN -> NewzroomRole.ADMIN
}

