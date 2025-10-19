package com.example.todoapp.core.webhook

import com.example.todoapp.TestUtils.testLog
import com.example.todoapp.core.webhook.Webhook.Companion.UNBRANDED_MSG_ID_KEY
import com.example.todoapp.core.webhook.Webhook.Companion.UNBRANDED_MSG_SIGNATURE_KEY
import com.example.todoapp.core.webhook.Webhook.Companion.UNBRANDED_MSG_TIMESTAMP_KEY
import com.example.todoapp.core.webhook.exception.WebhookVerificationException
import com.example.todoapp.core.webhook.properties.WebhookSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import kotlin.test.*

@SpringBootTest
class WebhookTest @Autowired constructor(webhookRegistry: WebhookRegistry) {
	val supabaseAuthWebhook = webhookRegistry[WebhookSource.SUPABASE]

	private val logger = LoggerFactory.getLogger(WebhookTest::class.java)

	@Test
	fun `should successfully verify a valid webhook request`() {
		// given
		val payload = """{"message":"hello world"}"""
		val webhookId = "wh_12345"
		val timestamp = System.currentTimeMillis() / 1000

		val signature = supabaseAuthWebhook.sign(webhookId, timestamp, payload)
		testLog("Generated signature: $signature")

		val headers = HttpHeaders().apply {
			add(UNBRANDED_MSG_ID_KEY, webhookId)
			add(UNBRANDED_MSG_TIMESTAMP_KEY, timestamp.toString())
			add(UNBRANDED_MSG_SIGNATURE_KEY, signature)
		}

		// when & then
		// No extension should be thrown
		supabaseAuthWebhook.verify(payload, headers)
		logger.debug("Successfully verified a valid webhook request.")
	}

	@Test
	fun `should throw exception for an invalid webhook signature`() {
		// given
		val payload = """{"message":"hello world"}"""
		val webhookId = "wh_12345"
		val timestamp = System.currentTimeMillis() / 1000

		val headers = HttpHeaders().apply {
			add(UNBRANDED_MSG_ID_KEY, webhookId)
			add(UNBRANDED_MSG_TIMESTAMP_KEY, timestamp.toString())
			add(UNBRANDED_MSG_SIGNATURE_KEY, "v1,invalid_signature")
		}

		// when & then
		assertFailsWith<WebhookVerificationException>("No matching signature found") {
			supabaseAuthWebhook.verify(payload, headers)
		}
		logger.debug("Correctly failed to verify a webhook with an invalid signature.")
	}
}