package com.example.todoapp.app.auth.controller

import com.example.todoapp.app.auth.model.JwtAuthHookRequest
import com.example.todoapp.core.webhook.WebhookRegistry
import com.example.todoapp.core.webhook.properties.WebhookProperties
import jdk.internal.joptsimple.internal.Messages.message
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@RestController
@RequestMapping("/auth/hooks")
class AuthWebhookController(
	private val jsonMapper : JsonMapper,
	webhookRegistry: WebhookRegistry,
) {

	private val logger = LoggerFactory.getLogger(AuthWebhookController::class.java)
	val supabaseAuthWebhook = webhookRegistry[WebhookProperties.Source.SUPABASE]

	@PostMapping("/custom_access_token")
	suspend fun getCustomJwt(
		@RequestBody payload: String,
		@RequestHeader headers: HttpHeaders
	): ResponseEntity<Any> {
		runCatching {
			supabaseAuthWebhook.verifyAndDedupe(payload, headers)
		}.onFailure { throwable ->
			return ResponseEntity.status(400)
				.contentType(MediaType.TEXT_PLAIN)
				.body(throwable.message ?: "Webhook Verification Failed"
				)
		}

		val request = jsonMapper.readValue<JwtAuthHookRequest>(payload)
		return ResponseEntity.ok(request)
	}
}