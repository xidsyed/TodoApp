package com.example.todoapp.app.invitation.mapper

import com.example.todoapp.app.auth.roles.data.mapper.dto
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.model.InvitationDto
import com.example.todoapp.app.users.model.UserDto

fun  InvitationEntity.dto(assignor: UserDto, assignee: UserDto?)  = InvitationDto(
	id = this.id!!,
	email = this.email,
	assignor = assignor,
	assignee = assignee,
	role = this.role.dto(),
	eat = this.eat,
	createdAt = this.createdAt,
)