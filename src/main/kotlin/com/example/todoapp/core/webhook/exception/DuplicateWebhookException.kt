package com.example.todoapp.core.webhook.exception

import java.lang.RuntimeException

class DuplicateWebhookException (id : String, cause : Throwable? = null) : RuntimeException("Duplicate Webhook Id : $id ", cause)