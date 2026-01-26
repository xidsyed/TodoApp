package com.example.todoapp.app.auth

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import com.example.todoapp.common.spring.auth.AuthenticationManagerWithJwtFilter
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.stereotype.Component

/**
 * Reactive JWT-based [ReactiveAuthenticationManager] implementation that integrates the application's
 * custom [JwtFilterService] for additional filtering or token handling logic.
 *
 * This component delegates to a fully configured [JwtReactiveAuthenticationManager] which performs
 * validation and signature checks on incoming JWT tokens using the provided [ReactiveJwtDecoder].
 *
 * ## Responsibilities
 * - Decode and validate incoming JWT tokens.
 * - Convert valid JWTs into authenticated `Authentication` objects with granted authorities.
 * - Apply custom post-authentication logic via [JwtFilterService] (e.g., revocation checks or blacklisting).
 *
 * ## Composition
 * This class is a thin wrapper that delegates all behavior to an internal instance created by
 * [buildJwtAuthenticationManager], allowing it to be injected as a Spring-managed component anywhere
 * a [ReactiveAuthenticationManager] is required.
 *
 * @property jwtFilterService custom service for additional token filtering (revocation, expiry, etc.)
 * @property reactiveJwtDecoder decoder used to validate and parse JWT tokens
 *
 * @see JwtReactiveAuthenticationManager
 * @see ReactiveJwtDecoder
 * @see AuthenticationManagerWithJwtFilter
 */

@Component
class JwtAuthenticationManager(jwtFilterService: JwtFilterService, reactiveJwtDecoder: ReactiveJwtDecoder) :
	ReactiveAuthenticationManager by buildJwtAuthenticationManager(jwtFilterService, reactiveJwtDecoder)

private val logger = LoggerFactory.getLogger(JwtAuthenticationManager::class.java)

private fun buildJwtAuthenticationManager(
	jwtFilterService: JwtFilterService,
	reactiveJwtDecoder: ReactiveJwtDecoder
): ReactiveAuthenticationManager {
	val grantedAuthoritiesConverter = Converter<Jwt, Collection<GrantedAuthority>> { jwt ->
		val role = runCatching {
			jwt.claims["app_role"]
				.toString()
				.lowercase()
		}.getOrNull() ?: "none"
		listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
	}

	val delegate = JwtAuthenticationConverter().apply {
		setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
	}

	val jwtAuthConverter = ReactiveJwtAuthenticationConverterAdapter(delegate)

	val jwtAuthenticationManager = JwtReactiveAuthenticationManager(reactiveJwtDecoder).apply {
		setJwtAuthenticationConverter(jwtAuthConverter)
	}

	return AuthenticationManagerWithJwtFilter(
		delegate = jwtAuthenticationManager, jwtFilterService = jwtFilterService
	)
}
