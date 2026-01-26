package com.example.todoapp.app.auth.hooks.model

import java.time.Instant
import java.util.*

data class BeforeUserCreatedPayload(
	val metadata: Metadata,
	val user: User
) {
	data class Metadata(
		val uuid: UUID,
		val time: Instant,
		val ipAddress: String,
		val name: String // Always "before-user-created"
	)

	data class User(
		val id: UUID,
		val aud: String,
		val role: String,
		val email: String,
		val phone: String,
		val appMetadata: AppMetadata,
		val userMetadata: UserMetadata,
		val identities: List<Map<String, Any?>>,
		val createdAt: Instant,
		val updatedAt: Instant,
		val isAnonymous: Boolean
	) {

		data class UserMetadata(
			val avatarUrl : String,
			val email: String,
			val fullName : String,		)

		data class AppMetadata(
			val provider: String,
			val providers: List<String>
		)
	}
}
