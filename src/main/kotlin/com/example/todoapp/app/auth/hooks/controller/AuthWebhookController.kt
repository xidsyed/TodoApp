package com.example.todoapp.app.auth.hooks.controller

import com.example.todoapp.app.auth.hooks.model.*
import com.example.todoapp.app.auth.hooks.service.AuthWebhookService
import com.example.todoapp.common.util.*
import com.example.todoapp.core.webhook.exception.*
import com.github.michaelbull.result.fold
import org.springframework.http.*
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/hooks")
class AuthWebhookController(
	private val authWebhookService: AuthWebhookService,
) {

	private val log = logger()

	@PostMapping("/custom_access_token")
	suspend fun postCustomAccessToken(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders,
	): ResponseEntity<*> {
		return authWebhookService.processCustomAccessTokenHook(payload, headers).fold(
			success = {
				ok(it)
			},
			failure = { err ->
				when (err) {
					is UserInvitationError, is UserIsFrozen -> throw err(
						HttpStatus.FORBIDDEN,
						err.message
					)

					is WebhookError -> handleWebhookError(err)
				}
			}
		)
	}


	@PostMapping("/before_user_created")
	suspend fun postBeforeUserCreated(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders
	): ResponseEntity<Any> {
		log.info("Received before_user_created webhook request")
		return authWebhookService.processBeforeUserCreatedHook(payload, headers).fold(
			success = {
				ok("")
			},
			failure = { err ->
				when (err) {
					is UserInvitationError -> throw err(HttpStatus.FORBIDDEN, err.message)
					is WebhookError -> handleWebhookError(err)
				}
			}
		)
	}

	private fun handleWebhookError(err: WebhookError): ResponseEntity<Any> {
		return when (val webhookEx = err.ex) {
			is DuplicateWebhookException -> {
				log.warn("UNEXPECTED. Exception should be unreachable, swallowed by upstream function")
				ok("")
			}

			is WebhookSourceNotFoundException, is WebhookSigningException -> throw err(
				HttpStatus.INTERNAL_SERVER_ERROR, webhookEx.message
			)

			is WebhookVerificationException -> throw err(HttpStatus.UNAUTHORIZED, err.message)
		}
	}
}
