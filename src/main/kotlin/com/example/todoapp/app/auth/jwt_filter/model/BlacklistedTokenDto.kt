package com.example.todoapp.app.auth.jwt_filter.model

data class BlacklistedTokenDto(
	val sub: String,
	val blacklistedAt: Long
)