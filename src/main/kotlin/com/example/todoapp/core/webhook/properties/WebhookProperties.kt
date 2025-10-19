package com.example.todoapp.core.webhook.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "webhook")
data class WebhookProperties (
	val sources : Map<WebhookSource, String>,
){
}

enum class WebhookSource(value : String) {
	SUPABASE("supabase")
}