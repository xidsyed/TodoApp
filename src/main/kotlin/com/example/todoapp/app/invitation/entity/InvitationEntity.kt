package com.example.todoapp.app.invitation.entity

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Table("invitation")
data class InvitationEntity(
	@Id
    var id: UUID? = null,
	val email: String,
	@Column("created_by")
    val createdBy: UUID,
	val role: NewzroomRoleEntity,
	val eat: Instant,
	@Column("revoked_at")
    val revokedAt: Instant? = null,
	@Column("assigned_to")
    val assignedTo: UUID? = null,
	@Column("created_at")
    val createdAt: Instant = Instant.now().truncatedTo(ChronoUnit.MICROS),
	@Column("updated_at")
    val updatedAt: Instant = Instant.now().truncatedTo(ChronoUnit.MICROS)
)
