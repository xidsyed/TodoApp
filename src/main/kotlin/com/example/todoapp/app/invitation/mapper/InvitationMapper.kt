package com.example.todoapp.app.invitation.mapper

import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.model.InvitationDto
import com.example.todoapp.app.users.model.UserDto
import com.example.todoapp.common.mapper.dto

fun  InvitationEntity.dto(createdBy: UserDto, assignedTo: UserDto?)  = InvitationDto(
	id = this.id,
	email = this.email,
	createdBy = createdBy,
	assignedTo = assignedTo,
	role = this.role.dto(),
	eat = this.eat,
	revokedAt = this.revokedAt,
	createdAt = this.createdAt,
)