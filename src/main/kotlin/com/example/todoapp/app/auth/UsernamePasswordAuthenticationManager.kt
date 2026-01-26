package com.example.todoapp.app.auth

import com.example.todoapp.app.auth.management.ManagementUsersProperties
import org.springframework.security.authentication.*
import org.springframework.security.core.userdetails.*
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.stereotype.Component

/**
 * Reactive username/password [ReactiveAuthenticationManager] used for Basic or form-based authentication flows.
 *
 * This manager uses an in-memory [MapReactiveUserDetailsService] with a set of users defined in the application's
 * configuration via [ManagementUsersProperties]. It is intended for protecting administrative or documentation
 * endpoints (e.g., Swagger UI).
 *
 * ## Responsibilities
 * - Authenticate incoming username/password credentials via HTTP Basic or form login.
 * - Hash and compare credentials securely using the configured [PasswordEncoder].
 * - Provide a minimal, self-contained authentication setup independent of the main JWT flow.
 *
 * ## Usage
 * Typically wired into a dedicated security chain (e.g., for `/swagger-ui/` and `/v3/api-docs/`),
 * while the rest of the application uses JWT-based authentication.
 *
 * @see ManagementUsersProperties
 **/

@Component
class UsernamePasswordAuthenticationManager(props: ManagementUsersProperties) :
	ReactiveAuthenticationManager by init(props)

private fun init(
	props: ManagementUsersProperties
): ReactiveAuthenticationManager {
	val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

	val users = props.management.flatMap { (role, users) ->
		users.map { credentials ->
			User.withUsername(credentials.username)
				.password(passwordEncoder.encode(credentials.password))
				.roles(role.value)
				.build()
		}
	}

	val userDetailsService = MapReactiveUserDetailsService(users)
	val authManager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
	authManager.setPasswordEncoder(passwordEncoder) // important so raw vs encoded compare works
	return authManager
}
