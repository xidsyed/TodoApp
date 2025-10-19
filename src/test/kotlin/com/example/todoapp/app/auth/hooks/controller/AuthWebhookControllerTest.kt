package com.example.todoapp.app.auth.hooks.controller

import com.example.todoapp.app.auth.hooks.model.JwtPayload
import com.example.todoapp.core.webhook.*
import com.example.todoapp.core.webhook.properties.WebhookSource
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
    private val supabaseAuthWebhook = webhookRegistry[WebhookSource.SUPABASE]

    @Test
    fun `should return 200 OK and body for a valid webhook request` () {
        // given
        val request = JwtPayload(
            userId = "user-id-123",
            claims= JwtPayload.Claims(
				iss = "test",
				sub = "user-id-123",
				aud = "authenticated",
				exp = Clock.systemUTC().instant().plus(10, ChronoUnit.DAYS).epochSecond.toInt(),
				iat = Clock.systemUTC().instant().epochSecond.toInt(),
				aal = "aal1",
				email = "test@example.com",
				phone = "",
				role = "authenticated",
				sessionId = "random_string",
				isAnonymous = false,
				appMetadata = JwtPayload.Claims.AppMetadata(
					provider = "email",
					providers = listOf("email")
				),
				userMetadata = JwtPayload.Claims.UserMetadata(
					avatarUrl = "https://example.com/avatar.png",
					fullName = "Test User",
					picture = "https://example.com/avatar.png"
				)
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
		val request = JwtPayload(
			userId = "user-id-123",
			claims= JwtPayload.Claims(
				iss = "test",
				sub = "user-id-123",
				aud = "authenticated",
				exp = Clock.systemUTC().instant().plus(10, ChronoUnit.DAYS).epochSecond.toInt(),
				iat = Clock.systemUTC().instant().epochSecond.toInt(),
				aal = "aal1",
				email = "test@example.com",
				phone = "",
				role = "authenticated",
				sessionId = "random_string",
				isAnonymous = false,
				appMetadata = JwtPayload.Claims.AppMetadata(
					provider = "email",
					providers = listOf("email")
				),
				userMetadata = JwtPayload.Claims.UserMetadata(
					avatarUrl = "https://example.com/avatar.png",
					fullName = "Test User",
					picture = "https://example.com/avatar.png"
				)
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