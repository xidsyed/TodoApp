package com.example.todoapp.app.auth.hooks.model

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole

data class JwtPayload(
	val authenticationMethod: String,
	val claims: Claims,
	val userId: String
) {
	data class Claims(
		val iss: String,
		val sub: String,
		val aud: String,
		val exp: Int,
		val iat: Int,
		val aal: String,
		val email: String,
		val phone: String,
		val role: String,
		val sessionId: String,
		val isAnonymous: Boolean,
		val amr: List<Amr>? = null,
		val appMetadata: AppMetadata,
		val userMetadata: UserMetadata,
		val appRole: NewzroomRole?=null,
	) {
		data class Amr(
			val method: String,
			val timestamp: Int
		)

		data class AppMetadata(
			val provider: String,
			val providers: List<String>,
		)

		data class UserMetadata(
			val avatarUrl: String,
			val fullName: String,
			val picture: String,
			val signupPayload: SignupPayload? = null
		) {
			data class SignupPayload(
				val invitationToken: String? = null
			)
		}
	}
}