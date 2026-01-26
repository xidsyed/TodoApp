package com.example.todoapp.app.users.entity

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import org.springframework.data.annotation.*
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.util.*

@Table("user_profiles")
data class UserEntity(
	@Id
	@Column("id")
	val userId: UUID,
	@Column("email")
	val email: String,
	@Column("created_at")
	val createdAt: Instant? = null,
	@Column("updated_at")
	val updatedAt: Instant? = null,
	@Column("freeze_till")
	val freezeTill: Instant? = null,
	@Column("freeze_cause")
	val freezeCause: String? = null,
	@Column("display_name")
	val displayName: String,
	@Column("profile_pic")
	val profilePic: String? = null,
	@Column("role")
	val role: NewzroomRoleEntity,
) : Persistable<UUID> {
	@Transient
	var isNewRecord: Boolean = (createdAt == null)

	override fun getId(): UUID = userId
	override fun isNew(): Boolean = isNewRecord
}
