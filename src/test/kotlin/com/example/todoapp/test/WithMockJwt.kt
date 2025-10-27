package com.example.todoapp.test

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.*


/**
 * A meta-annotation for Spring Security tests to mock a JWT-authenticated user.
 *
 * This annotation simplifies testing of protected endpoints by creating a `JwtAuthenticationToken`
 * and setting it in the `SecurityContext`. It allows specifying the user's subject (ID) and role.
 *
 * Usage:
 * ```
 * @Test
 * @WithMockJwt(subject = "user-id-123", role = NewzroomRole.WRITER)
 * fun myTest() {
 *     // The test will run with a mock authenticated user.
 * }
 * ```
 *
 * @param subject The subject (sub) claim of the JWT, representing the user's ID. Defaults to an empty string.
 * @param uuid A string representation of a UUID to be used as the subject. If provided, this overrides the `subject` parameter.
 * @param role The role of the user, used for authorization. Defaults to `NewzroomRole.ADMIN`.
 */
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwt.WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(
	val subject: String = "",
	val uuid: String = "",
	val role: NewzroomRole = NewzroomRole.ADMIN
) {
	class WithMockJwtSecurityContextFactory : WithSecurityContextFactory<WithMockJwt> {
		override fun createSecurityContext(annotation: WithMockJwt): SecurityContext {
			val context = SecurityContextHolder.createEmptyContext()
			val subject = if (annotation.uuid.isNotEmpty()) annotation.uuid else annotation.subject
			val jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject(subject)
				.claim("app_role", annotation.role.value.lowercase())
				.build()
			val authorities = listOf(SimpleGrantedAuthority("ROLE_${annotation.role.value.uppercase()}"))
			context.authentication = JwtAuthenticationToken(jwt, authorities).apply { isAuthenticated = true }
			return context
		}
	}
}
