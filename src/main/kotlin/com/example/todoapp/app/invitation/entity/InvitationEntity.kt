package com.example.todoapp.app.invitation.entity

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Table("invitations")
data class InvitationEntity(
	@Id
    var id: UUID? = null,
	val email: String,
	@Column("assignor")
    val assignor: UUID,
	val role: NewzroomRoleEntity,
	val eat: Instant,
	@Column("assignee")
    val assignee: UUID? = null,
	@Column("created_at")
    val createdAt: Instant = Instant.now().truncatedTo(ChronoUnit.MICROS),
	@Column("updated_at")
    val updatedAt: Instant = Instant.now().truncatedTo(ChronoUnit.MICROS)
)
