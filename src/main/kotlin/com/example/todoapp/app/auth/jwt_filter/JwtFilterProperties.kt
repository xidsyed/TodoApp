package com.example.todoapp.app.auth.jwt_filter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt-filter")
data class JwtFilterProperties (
	val jwtExpirationDurationInSeconds : Long,
)