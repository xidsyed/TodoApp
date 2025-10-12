package com.example.todoapp.app.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class JwtAuthHookRequest(
	val authenticationMethod: String,
	val claims: Claims,
	val userId: String
) {
    data class Claims(
		val aal: String? = null,
		val amr: List<Amr>? = null,
		val appMetadata: Map<String, Any>? = null,
		val aud: String,
		val email: String,
		val exp: Int,
		val iat: Int,
		val isAnonymous: Boolean,
		val phone: String? = null,
		val role: String,
		val sessionId: String? = null,
		val sub: String,
		val userMetadata: Map<String, Any>? = null
    ) {
        data class Amr(
            val method: String,
            val timestamp: Int
        )
    }
}