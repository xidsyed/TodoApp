package com.example.todoapp.common.spring.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class GrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {
	override fun convert(source: Jwt): Collection<GrantedAuthority> {
		val role = source.getClaimAsString("app_role")?.lowercase() ?: "none"
		return listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
	}
}