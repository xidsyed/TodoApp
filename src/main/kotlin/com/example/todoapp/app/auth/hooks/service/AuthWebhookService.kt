package com.example.todoapp.app.auth.hooks.service

import com.example.todoapp.app.auth.hooks.model.*
import com.example.todoapp.app.auth.roles.data.mapper.dto
import com.example.todoapp.app.invitation.InvitationRepository
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.exception.*
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.core.extensions.executeAndAwaitResult
import com.example.todoapp.core.webhook.WebhookRegistry
import com.example.todoapp.core.webhook.exception.*
import com.example.todoapp.core.webhook.properties.WebhookSource
import com.github.michaelbull.result.*
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.time.Instant

@Service
class AuthWebhookService(
	private val userRepository: UserRepository,
	private val invitationRepo: InvitationRepository,
	private val jsonMapper: JsonMapper,
	private val txOperator: TransactionalOperator,
	webhookRegistry: WebhookRegistry,
) {

	val supabaseAuthWebhook = webhookRegistry[WebhookSource.SUPABASE]

	suspend fun processBeforeUserCreatedHook(
		payload: String,
		headers: HttpHeaders
	): Result<Unit, BeforeUserCreatedTokenProcessingError> {
		val body = jsonMapper.readValue<BeforeUserCreatedPayload>(payload)
		return verifyWebhook(payload, headers).andThen {
			fetchValidUserInvitation(body.user.email).map { }.mapError { ex -> UserInvitationError(ex) }
		}
	}

	suspend fun processCustomAccessTokenHook(
		payload: String,
		headers: HttpHeaders,
	): Result<JwtPayload, CustomTokenProcessingError> {
		val jwt = jsonMapper.readValue<JwtPayload>(payload)
		return verifyWebhook(payload, headers)
			.andThen {
				fetchOrCreateUserProfile(jwt)
			}.andThen { user ->
				if (user.freezeTill != null && user.freezeTill >= Instant.now()) {
					Err(UserIsFrozen(user.freezeTill, user.freezeCause))
				} else {
					val responsePayload = jwt.run { copy(claims = claims.copy(appRole = user.role.dto())) }
					Ok(responsePayload)
				}
			}
	}

	private suspend inline fun verifyWebhook(
		payload: String,
		headers: HttpHeaders
	): Result<Unit, WebhookError> {
		// return result wrapped unit error type
		return runCatching<Result<Unit, WebhookError>> {
			supabaseAuthWebhook.verifyAndDedupe(payload, headers)
			Ok(Unit)
		}.getOrElse { ex ->
			when (ex) {
				is DuplicateWebhookException -> Ok(Unit) // Swallow for idempotency
				is WebhookException -> Err(WebhookError(ex))
				else -> throw ex
			}
		}
	}


	private suspend inline fun fetchOrCreateUserProfile(jwt: JwtPayload): Result<UserEntity, CustomTokenProcessingError> =
		txOperator.executeAndAwaitResult {
			val fetchedUser = userRepository.findById(jwt.uuid)
			if (fetchedUser != null) return@executeAndAwaitResult Ok(fetchedUser)

			// try to create profile from invitation and jwt
			fetchValidUserInvitation(jwt.claims.email)
				.mapError { invitationEx -> UserInvitationError(invitationEx) }
				.andThen { invitation ->
					val createdUser = userRepository.save(
						UserEntity(
							userId = jwt.uuid,
							displayName = jwt.claims.userMetadata.fullName,
							role = invitation.role,
							profilePic = jwt.claims.userMetadata.avatarUrl,
							email = jwt.claims.email
						)
					)
					invitationRepo.save(invitation.copy(assignee = jwt.uuid))
					Ok(createdUser)
				}
		}

	private suspend fun fetchValidUserInvitation(email: String): Result<InvitationEntity, InvitationException> {
		val userInvitation = invitationRepo.findByEmail(email)
			?: return Err(NoInvitationFoundForEmail(email))

		return userInvitation.let {
			when {
				it.eat <= Instant.now() -> Err(InvitationExpired(it.eat))
				it.assignee != null -> Err(InvitationAlreadyAssigned())
				else -> Ok(it)
			}
		}
	}

}
