package com.example.todoapp.app.users.entity

import com.example.todoapp.common.data.entity.NewzroomRoleEntity
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("newzroom_user_profiles")
data class UserEntity(
	@Id
	@Column("id")
    val userId: UUID,
	@Column("created_at")
    val createdAt: Instant,
	@Column("updated_at")
    val updatedAt: Instant,
	@Column("freeze_till")
    val freezeTill: Long? = null,
	@Column("freeze_cause")
    val freezeCause: String? = null,
	@Column("display_name")
    val displayName: String,
	@Column("profile_pic")
    val profilePic: String? = null,
	@Column("role")
    val role: NewzroomRoleEntity,
) : Persistable<UUID> {
	override fun getId(): UUID = userId
	override fun isNew(): Boolean = true
}
