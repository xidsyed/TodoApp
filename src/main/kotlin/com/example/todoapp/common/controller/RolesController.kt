package com.example.todoapp.common.controller

import com.example.todoapp.app.auth.roles.annotations.*
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
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

	@GetMapping("/current_role")
	suspend fun rolesCurrentRole(@CurrentRole role: NewzroomRole): ResponseEntity<Map<String, String>> {
		return ok(mapOf("role" to role.name))
	}

}
