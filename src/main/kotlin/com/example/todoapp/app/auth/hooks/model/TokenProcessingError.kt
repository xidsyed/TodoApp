package com.example.todoapp.app.auth.hooks.model

import com.example.todoapp.app.invitation.exception.InvitationException
import com.example.todoapp.core.webhook.exception.WebhookException
import java.time.Instant

sealed interface CustomTokenProcessingError {
	val message: String
}

sealed interface BeforeUserCreatedTokenProcessingError {
	val message: String
}

data class UserIsFrozen(
	val freezeTill: Instant,
	val freezeCause: String? = null,
) : CustomTokenProcessingError {
	override val message: String = "User is frozen till $freezeTill" + (freezeCause?.let { " because of $it" } ?: "")
}


data class UserInvitationError(val ex: InvitationException, override val message: String = ex.message) :
	CustomTokenProcessingError,
	BeforeUserCreatedTokenProcessingError

data class WebhookError(val ex: WebhookException) : CustomTokenProcessingError,
	BeforeUserCreatedTokenProcessingError {
	override val message: String = "Webhook validation failed: ${ex.message}"
}
