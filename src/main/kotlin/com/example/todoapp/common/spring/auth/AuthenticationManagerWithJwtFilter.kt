package com.example.todoapp.common.spring.auth

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import reactor.core.publisher.Mono
import java.time.Instant

class AuthenticationManagerWithJwtFilter(
	private val delegate: JwtReactiveAuthenticationManager,
	private val jwtFilterService: JwtFilterService
) : ReactiveAuthenticationManager {

	override fun authenticate(authentication: Authentication): Mono<Authentication> {
		// delegate.authenticate will validate the JWT and convert it to an Authentication (with Jwt principal)
		return delegate.authenticate(authentication)
			.flatMap { auth ->
				val jwt = auth.principal as Jwt
				val tokenId = jwt.claims["sub"] as String
				val iat = jwt.claims["iat"] as Instant
				mono {
					val isBlacklisted = jwtFilterService.isTokenValid(tokenId, iat)
					if (isBlacklisted) throw OAuth2AuthenticationException(
						OAuth2Error(
							"invalid_token",
							"access_token has been blacklisted. please refresh your access token",
							null
						)
					)
					auth
				}
			}
	}
}