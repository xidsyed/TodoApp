package com.example.todoapp.app.users.model

import com.example.todoapp.app.auth.roles.data.mapper.entity
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.validation.AtLeastOneFieldNotNull
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.URL
import java.time.Instant

@AtLeastOneFieldNotNull
data class InternalPatchUserRequest(
	@field:NotBlank
	val name: String? = null,
	@field:URL
	val picture: String? = null,
	@field:Future
	val freezeTill: Instant? = null,
	val freezeCause: String? = null,
	val role: NewzroomRole? = null
)

fun UserEntity.applyPatch(patch: InternalPatchUserRequest) = copy(
	displayName = patch.name ?: displayName,
	profilePic = patch.picture ?: profilePic,
	freezeTill = patch.freezeTill ?: freezeTill,
	freezeCause = patch.freezeCause,
	role = patch.role?.entity() ?: role
)
