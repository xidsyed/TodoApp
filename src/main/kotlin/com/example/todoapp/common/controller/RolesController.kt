package com.example.todoapp.common.controller

import com.example.todoapp.app.auth.roles.annotations.*
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@EnableReactiveMethodSecurity
@RequestMapping("/roles")
class RolesController {

	@RequireRole
	@GetMapping("/valid_role")
	suspend fun rolesValidRole(): ResponseEntity<String> = ok("valid_role")

	@RequireAdmin
	@GetMapping("/admin")
	suspend fun rolesAdmin(): ResponseEntity<String> = ok("")

	@GetMapping("/admin/id")
	suspend fun rolesAdminId(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<String> =
		ok(jwt.claims["sub"].toString())

	@GetMapping("/current_role")
	suspend fun rolesCurrentRole(@CurrentRole role: NewzroomRole): ResponseEntity<Map<String, String>> {
		return ok(mapOf("role" to role.name))
	}

	@GetMapping("/authenticated_only")
	@PreAuthorize("isAuthenticated()")
	suspend fun authenticatedOnly(): ResponseEntity<String> = ok("authenticated")

}
