package com.example.todoapp.app.auth.hooks.controller

import com.example.todoapp.app.auth.hooks.model.JwtPayload
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.core.webhook.WebhookRegistry
import com.example.todoapp.core.webhook.properties.WebhookSource
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@RestController
@RequestMapping("/auth/hooks")
class AuthWebhookController(
	private val jsonMapper: JsonMapper,
	webhookRegistry: WebhookRegistry,
) {

	private val logger = LoggerFactory.getLogger(AuthWebhookController::class.java)
	val supabaseAuthWebhook = webhookRegistry[WebhookSource.SUPABASE]

	@PostMapping("/custom_access_token")
	suspend fun postCustomAccessToken(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders
	): ResponseEntity<Any> {
		supabaseAuthWebhook.verifyAndDedupe(payload, headers)
		val request = jsonMapper.readValue<JwtPayload>(payload)
		return ResponseEntity.ok(request.copy(claims = request.claims.copy(appRole = NewzroomRole.ADMIN)))
	}

	@PostMapping("/before_user_created")
	suspend fun postBeforeUserCreated(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders
	): ResponseEntity<Any> {
		supabaseAuthWebhook.verifyAndDedupe(payload, headers)
		val request = jsonMapper.readValue<JwtPayload>(payload)


		// check if invitation token is valid
		/*
		return ResponseEntity
			.status(HttpStatus.FORBIDDEN)
			.contentType(MediaType.APPLICATION_JSON)
			.body(mapOf("error" to "Invalid invitation token"))
		*/
		return ResponseEntity.ok("user verified")
	}

}
