package com.example.todoapp.app.invitation.exception

import java.time.Instant

sealed class InvitationException(override val message: String) : RuntimeException(message)

data class InvitationExpired(
	val expiration: Instant,
	override val message: String = "User invitation expired at $expiration"
) : InvitationException(message)

data class NoInvitationFoundForEmail(
	val email: String,
	override val message: String = "User invitation for email $email not found"
) : InvitationException(message)

data class InvitationAlreadyAssigned(
	override val message: String = "Invitation has already been assigned"
) : InvitationException(message)

