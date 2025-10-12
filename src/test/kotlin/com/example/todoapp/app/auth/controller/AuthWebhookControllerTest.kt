package com.example.todoapp.app.auth.controller

import com.example.todoapp.app.auth.model.JwtAuthHookRequest
import com.example.todoapp.core.webhook.*
import com.example.todoapp.core.webhook.config.WebhookProperties
import com.example.todoapp.core.webhook.properties.WebhookProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.databind.json.JsonMapper
import java.time.Clock
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthWebhookControllerTest @Autowired constructor(
	private val client: WebTestClient,
	private val jsonMapper : JsonMapper,
	webhookRegistry: WebhookRegistry
) {
    private val supabaseAuthWebhook = webhookRegistry[WebhookProperties.Source.SUPABASE]

    @Test
    fun `should return 200 OK and body for a valid webhook request`() {
        // given
        val request = JwtAuthHookRequest(
            userId = "user-id-123",
            claims= JwtAuthHookRequest.Claims(
				aud = "authenticated",
				role = "authenticated",
				email = "test@example.com",
				iat = Clock.systemUTC().instant().epochSecond.toInt(),
				exp = Clock.systemUTC().instant().plus(10, ChronoUnit.DAYS).epochSecond.toInt(),
				isAnonymous = false,
				sessionId = "random_string",
				sub = "user-id-123",
			),
			authenticationMethod = "password"
        )
        val payload = jsonMapper.writeValueAsString(request)
        val webhookId = "wh_test_123"
        val timestamp = Clock.systemUTC().instant().epochSecond
        val signature = supabaseAuthWebhook.sign(webhookId, timestamp, payload)

        // when & then
        client.post().uri("/auth/hooks/custom_access_token")
            .contentType(MediaType.APPLICATION_JSON)
            .header(Webhook.UNBRANDED_MSG_ID_KEY, webhookId)
            .header(Webhook.UNBRANDED_MSG_TIMESTAMP_KEY, timestamp.toString())
            .header(Webhook.UNBRANDED_MSG_SIGNATURE_KEY, signature)
            .bodyValue(payload)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should return 400 Bad Request for an invalid webhook signature`() {
        // given
		val request = JwtAuthHookRequest(
			userId = "user-id-123",
			claims= JwtAuthHookRequest.Claims(
				aud = "authenticated",
				role = "authenticated",
				email = "test@example.com",
				iat = Clock.systemUTC().instant().epochSecond.toInt(),
				exp = Clock.systemUTC().instant().plus(10, ChronoUnit.DAYS).epochSecond.toInt(),
				isAnonymous = false,
				sessionId = "random_string",
				sub = "user-id-123",
			),
			authenticationMethod = "password"
		)
        val payload = jsonMapper.writeValueAsString(request)

        val webhookId = "wh_test_456"
        val timestamp = System.currentTimeMillis() / 1000
        val invalidSignature = "v1,this_is_not_a_valid_signature"

        // when & then
        client.post().uri("/auth/hooks/custom_access_token")
            .contentType(MediaType.APPLICATION_JSON)
            .header(Webhook.UNBRANDED_MSG_ID_KEY, webhookId)
            .header(Webhook.UNBRANDED_MSG_TIMESTAMP_KEY, timestamp.toString())
            .header(Webhook.UNBRANDED_MSG_SIGNATURE_KEY, invalidSignature)
            .bodyValue(payload)
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.TEXT_PLAIN)
            .expectBody(String::class.java).isEqualTo("No matching signature found")
    }
}