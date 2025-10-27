package com.example.todoapp.core.webhook.exception

sealed class WebhookException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

class WebhookSigningException(message: String) : WebhookException(message)

class DuplicateWebhookException(val payload: String, override val cause: Throwable? = null) :
	WebhookException("Duplicate Webhook Id : $payload ", cause)

class WebhookVerificationException(message: String) : WebhookException(message)

class WebhookSourceNotFoundException(message: String, cause: Throwable? = null) : WebhookException(message, cause)