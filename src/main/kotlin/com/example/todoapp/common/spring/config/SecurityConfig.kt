package com.example.todoapp.common.spring.config

import com.example.todoapp.app.auth.*
import com.example.todoapp.app.auth.management.ManagementUsersProperties.Role.SWAGGER
import org.springframework.context.annotation.*
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.*
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

	// SECURITY MATCHER &  PATH MATCHER

	@Bean
	@Order(1)
	fun managementApiSecurityFilterChain(
		http: ServerHttpSecurity, usernamePasswordAuthenticationManager: UsernamePasswordAuthenticationManager
	): SecurityWebFilterChain {
		http.securityMatcher(    // CRUCIAL!! all requests that don't match fall through to next filter chain
			ServerWebExchangeMatchers.pathMatchers(
				"/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml/**"
			)
		).httpBasic { }.csrf { it.disable() }.cors { customizer ->
				val config = CorsConfiguration().apply {
					allowedOriginPatterns = listOf("*")
					allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
					allowedHeaders = listOf("*")
					allowCredentials = true
					maxAge = 3600L
				}

				val source = UrlBasedCorsConfigurationSource()
				source.registerCorsConfiguration("/**", config)
				customizer.configurationSource(source)
			}.formLogin { it.disable() }.authenticationManager(usernamePasswordAuthenticationManager)
			.authorizeExchange { auth -> auth.anyExchange().hasRole(SWAGGER.name) }
		return http.build()
	}

	@Bean
	@Order(2)
	fun apiSecurityFilterChain(
		http: ServerHttpSecurity, jwtAuthManager: JwtAuthenticationManager
	): SecurityWebFilterChain {
		http.csrf { it.disable() }.httpBasic { it.disable() }.formLogin { it.disable() }.cors { customizer ->
			val config = CorsConfiguration().apply {
				allowedOrigins = listOf("http://localhost:5173")
				allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				allowedHeaders = listOf("*")
				allowCredentials = true
				maxAge = 3600L
			}

			val source = UrlBasedCorsConfigurationSource()
			source.registerCorsConfiguration("/**", config)
			customizer.configurationSource(source)
		}.authorizeExchange {
			it.pathMatchers("/hello/**").permitAll()
			it.pathMatchers("/roles/**").permitAll()
			it.pathMatchers("/internal/**").hasRole("ADMIN")
			it.pathMatchers("/api/newzroom/admin/**").hasRole("ADMIN")
			it.pathMatchers("/jwt_filter/**").hasRole("ADMIN")
			it.pathMatchers("/invitation/**").permitAll()
			it.pathMatchers("/auth/hooks/**").permitAll()
			it.anyExchange().denyAll()
		}.oauth2ResourceServer { oauth -> oauth.jwt { it.authenticationManager(jwtAuthManager) } }
		return http.build()
	}


	@Bean
	@Primary
			/**
			 * This Reactive Authentication Manager is declared `@Primary` gets autowired into spring-security.
			 * it can delegate requests to different authentication managers based on authentication type.
			 */

	fun primaryReactiveAuthenticationManager(
		jwtAuthManager: JwtAuthenticationManager,
		usernamePasswordAuthenticationManager: UsernamePasswordAuthenticationManager
	): ReactiveAuthenticationManager {
		return ReactiveAuthenticationManager { authentication ->
			when (authentication) {
				// username/password (Basic/Form)
				is UsernamePasswordAuthenticationToken -> {
					usernamePasswordAuthenticationManager.authenticate(authentication)
				}

				// bearer token (OAuth2/JWT)
				is JwtAuthenticationToken, is BearerTokenAuthenticationToken -> {
					jwtAuthManager.authenticate(authentication)
				}

				else -> jwtAuthManager.authenticate(authentication)
			}
		}
	}

}
