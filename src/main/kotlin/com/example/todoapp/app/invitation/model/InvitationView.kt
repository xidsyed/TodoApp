package com.example.todoapp.app.invitation.model

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import org.springframework.data.relational.core.mapping.Column
import java.time.Instant
import java.util.*

data class InvitationView(
	val id: UUID,
	val email: String,
	val role: NewzroomRoleEntity,
	val eat: Instant,
	@Column("created_at")
	val createdAt: Instant,
	@Column("assignor_id")
	val assignorId: UUID,
	@Column("assignor_name")
	val assignorName: String,
	@Column("assignor_picture")
	val assignorPicture: String?,
	@Column("assignor_role")
	val assignorRole: NewzroomRoleEntity,
	@Column("assignee_id")
	val assigneeId: UUID?,
	@Column("assignee_name")
	val assigneeName: String?,
	@Column("assignee_picture")
	val assigneePicture: String?,
	@Column("assignee_role")
	val assigneeRole: NewzroomRoleEntity?
)
