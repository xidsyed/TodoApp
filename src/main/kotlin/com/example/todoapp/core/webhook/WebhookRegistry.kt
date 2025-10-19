package com.example.todoapp.core.webhook

import com.example.todoapp.core.webhook.exception.WebhookSourceNotFoundException
import com.example.todoapp.core.webhook.properties.WebhookSource
import com.example.todoapp.core.webhook.properties.WebhookProperties
import org.springframework.stereotype.Component

@Component
class WebhookRegistry(properties: WebhookProperties, private val webhookIdCache: WebhookIdCache) {

	val webhooks: Map<WebhookSource, Webhook> =
		properties.sources.mapValues { (_, secret) -> Webhook(secret, webhookIdCache) }

	operator fun get(source: WebhookSource): Webhook {
		return webhooks[source] ?: throw WebhookSourceNotFoundException("Webhook source not found")
	}
}
