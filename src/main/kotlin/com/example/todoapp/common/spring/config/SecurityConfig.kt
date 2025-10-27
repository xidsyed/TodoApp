package com.example.todoapp.common.spring.config

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import com.example.todoapp.common.spring.auth.*
import org.springframework.context.annotation.*
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

	@Bean
	fun springSecurityFilterChain(
		http: ServerHttpSecurity,
		reactiveAuthenticationManager: ReactiveAuthenticationManager
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
			it.pathMatchers("/swagger-ui/**").permitAll() // TODO : GUARD THESE ENDPOINTS WITH A PASSWORD
			it.pathMatchers("/v3/api-docs/**").permitAll() // TODO : GUARD THESE ENDPOINTS WITH A PASSWORD
			it.pathMatchers("/v3/api-docs.yaml/**").permitAll() // TODO : GUARD THESE ENDPOINTS WITH A PASSWORD
			it.anyExchange().denyAll()
		}.oauth2ResourceServer { oauth -> oauth.jwt { it.authenticationManager(reactiveAuthenticationManager) } }
		return http.build()
	}

	@Bean
	fun jwtAuthConverter(): ReactiveJwtAuthenticationConverterAdapter {
		val delegate = JwtAuthenticationConverter().apply {
			setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesConverter())
		}
		return ReactiveJwtAuthenticationConverterAdapter(delegate)
	}

	@Bean
	fun reactiveAuthenticationManager(
		reactiveJwtDecoder: ReactiveJwtDecoder,
		jwtFilterService: JwtFilterService,
		jwtAuthConverter: ReactiveJwtAuthenticationConverterAdapter
	): ReactiveAuthenticationManager {
		val jwtAuthenticationManager = JwtReactiveAuthenticationManager(reactiveJwtDecoder).apply {
			setJwtAuthenticationConverter(jwtAuthConverter)
		}
		return AuthenticationManagerWithJwtFilter(
			delegate = jwtAuthenticationManager,
			jwtFilterService = jwtFilterService
		)
	}
}
