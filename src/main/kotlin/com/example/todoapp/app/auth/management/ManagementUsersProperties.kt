package com.example.todoapp.app.auth.management

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "secret")
data class ManagementUsersProperties (
	val management: Map<Role, List<Credentials>>
) {
	enum class Role(val value: String) {
		SWAGGER("SWAGGER")
	}

	data class Credentials(
		@field:NotBlank val username: String,
		@field:NotBlank val password: String
	)
}