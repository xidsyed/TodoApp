package com.example.todoapp.common.config

import com.example.todoapp.core.util.PrettyPrintJson
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.*
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
	private val prettyPrintJson: PrettyPrintJson
) {

	private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

	@Bean
	fun springSecurityFilterChain(
		http: ServerHttpSecurity,
	): SecurityWebFilterChain {
		http.csrf { it.disable() }
			.httpBasic { it.disable() }
			.formLogin { it.disable() }
			.cors { customizer ->
				val config = CorsConfiguration().apply {
					// Use the exact origin in dev; don't use "*" together with allowCredentials=true
					allowedOrigins = listOf("http://localhost:5173")
					allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
					allowedHeaders = listOf("*")
					allowCredentials = true
					maxAge = 3600L
				}

				val source = UrlBasedCorsConfigurationSource()
				source.registerCorsConfiguration("/**", config)
				customizer.configurationSource(source)
			}
			.authorizeExchange {
				it.pathMatchers("/hello/**").permitAll()
				it.pathMatchers("/roles/**").permitAll()
				it.pathMatchers("/api/newzroom/admin/**").hasRole("ADMIN")
				it.pathMatchers("/api/newzroom/**").hasAnyRole("ADMIN", "WRITER")
				it.pathMatchers("/invitation").permitAll()
				it.pathMatchers("/auth/hooks/custom_access_token").permitAll()
				it.anyExchange().denyAll()
			}.oauth2ResourceServer { oauth ->
				oauth.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()) }
			}
		return http.build()
	}

	@Bean
	fun jwtAuthConverter(): ReactiveJwtAuthenticationConverterAdapter {
		val grantedAuthoritiesConverter = Converter<Jwt, Collection<GrantedAuthority>> { jwt ->
			logger.info("jwt - role: ${jwt.claims["app_role"]}")
			val role = jwt.getClaimAsString("app_role")?.lowercase() ?: "none"
			listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
		}
		val delegate = JwtAuthenticationConverter().apply {
			setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
		}
		return ReactiveJwtAuthenticationConverterAdapter(delegate)
	}
}
