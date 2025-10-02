package com.example.todoapp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

	@Bean
	fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
		http.csrf { it.disable() }
			.httpBasic { it.disable() }
			.formLogin { it.disable() }
			.authorizeExchange {
				it.pathMatchers("/hello").permitAll()
				it.pathMatchers("/api/newzroom/admin/**").hasRole("ADMIN")
				it.pathMatchers("/api/newzroom/**").hasAnyRole("ADMIN", "WRITER")
				it.anyExchange().denyAll()
			}.oauth2ResourceServer { oauth ->
				oauth.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()) }
			}
		return http.build()
	}

	@Bean
	fun jwtAuthConverter(): ReactiveJwtAuthenticationConverterAdapter {
		val grantedAuthoritiesConverter = Converter<Jwt, Collection<GrantedAuthority>> { jwt ->
			val role = jwt.getClaimAsString("role")?.lowercase() ?: "authenticated"
			listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
		}

		val delegate = JwtAuthenticationConverter().apply {
			setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
		}
		return ReactiveJwtAuthenticationConverterAdapter(delegate)
	}
}
