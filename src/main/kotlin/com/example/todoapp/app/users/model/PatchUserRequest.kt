package com.example.todoapp.app.users.model

import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.validation.AtLeastOneFieldNotNull
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

@AtLeastOneFieldNotNull
data class PatchUserRequest(
	@field:NotBlank
	val name: String? = null,
	@field:URL
	val picture: String? = null,
)


fun UserEntity.applyPatch(patch: PatchUserRequest): UserEntity {
	return this.copy(
		displayName = patch.name ?: this.displayName,
		profilePic = patch.picture ?: this.profilePic,
	)
}