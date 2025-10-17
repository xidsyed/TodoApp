package com.example.todoapp.core.webhook.exception

import java.lang.RuntimeException

class DuplicateWebhookException (val payload : String, override val cause : Throwable? = null) : RuntimeException("Duplicate Webhook Id : $payload ", cause)