package com.example.todoapp.app.auth.hooks.controller

import com.example.todoapp.app.auth.hooks.model.CustomJwtHookRequest
import com.example.todoapp.core.webhook.WebhookRegistry
import com.example.todoapp.core.webhook.properties.WebhookProperties
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
	val supabaseAuthWebhook = webhookRegistry[WebhookProperties.Source.SUPABASE]

	@PostMapping("/custom_access_token")
	suspend fun postCustomAccessToken(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders
	): ResponseEntity<Any> {
		supabaseAuthWebhook.verifyAndDedupe(payload, headers)
		val request = jsonMapper.readValue<CustomJwtHookRequest>(payload)

		// return `payload` instead of mapped `request` object to ensure all properties are returned intact
		return ResponseEntity
			.status(HttpStatus.OK)
			.contentType(MediaType.APPLICATION_JSON)
			.body(payload)
	}

	@PostMapping("/before_user_created")
	suspend fun postBeforeUserCreated(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders
	): ResponseEntity<Any> {
		supabaseAuthWebhook.verifyAndDedupe(payload, headers)

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
