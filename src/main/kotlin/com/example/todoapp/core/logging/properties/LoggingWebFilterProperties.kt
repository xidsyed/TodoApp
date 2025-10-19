package com.example.todoapp.core.logging.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.logging")
data class LoggingWebFilterProperties(
    var enabled: Boolean = false,
    var maxBodyBytes: Int = 8192
)