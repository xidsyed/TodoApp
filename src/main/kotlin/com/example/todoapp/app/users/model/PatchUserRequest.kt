package com.example.todoapp.app.users.model

import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.validation.NullOrNotBlank

//@AtLeastOneFieldNotNull
data class PatchUserRequest(
	@field:NullOrNotBlank
	val name: String? = null,
	@field:NullOrNotBlank
	val picture: String? = null,	
)


fun UserEntity.applyPatch(patch: PatchUserRequest): UserEntity {
	return this.copy(
		displayName = patch.name ?: this.displayName,
		profilePic = patch.picture ?: this.profilePic,
	)
}