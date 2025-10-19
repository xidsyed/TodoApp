package com.example.todoapp.app.auth.roles.annotations

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CurrentRoleArgumentResolver : HandlerMethodArgumentResolver {
	private val logger = LoggerFactory.getLogger(CurrentRoleArgumentResolver::class.java)

	override fun supportsParameter(parameter: MethodParameter): Boolean {
		return parameter.hasParameterAnnotation(CurrentRole::class.java) &&
				parameter.parameterType == NewzroomRole::class.java
	}

	override fun resolveArgument(
		parameter: MethodParameter,
		bindingContext: BindingContext,
		exchange: ServerWebExchange
	): Mono<Any> {
		return ReactiveSecurityContextHolder.getContext()
			.map { ctx ->
				val auth = ctx.authentication
				val roleName = auth?.authorities
					?.firstOrNull()?.authority
					?.removePrefix("ROLE_")
					?.uppercase()

				val role = try {
					NewzroomRole.valueOf(roleName ?: "NO_ROLE_FOUND")
				} catch (e: IllegalArgumentException) {
					logger.warn("Invalid role name: $roleName, falling back to NONE")
					NewzroomRole.NONE
				}
				logger.info("Resolved role to: ${role.name}")
				role
			}
			.defaultIfEmpty(NewzroomRole.NONE)
			.doOnSuccess { role ->
				if (role == NewzroomRole.NONE) {
					logger.warn("No role found in security context, defaulting to NONE.")
				}
			}
			.cast(Any::class.java)
	}
}
