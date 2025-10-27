package com.example.todoapp.app.users

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import com.example.todoapp.app.auth.roles.annotations.*
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.app.users.exception.UserNotFoundEx
import com.example.todoapp.app.users.mapper.dto
import com.example.todoapp.app.users.model.*
import com.example.todoapp.common.exception.PermissionDeniedEx
import jakarta.validation.Valid
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@EnableReactiveMethodSecurity
@RequireRole
@RequestMapping("/users")
@Validated
class UserController(
	val userRepository: UserRepository,
	val jwtFilterService: JwtFilterService
) {
	@PatchMapping("/{id}")
	suspend fun patchUserById(
		@PathVariable id: UUID,
		@Valid @RequestBody patch: PatchUserRequest,
		@AuthenticationPrincipal jwt: Jwt,
		@CurrentRole currentRole: NewzroomRole
	): UserDto {
		val user = userRepository.findById(id) ?: throw UserNotFoundEx(id)
		val notAdmin = currentRole != NewzroomRole.ADMIN
		val notOwner = jwt.subject != id.toString()
		if (notAdmin && notOwner) {
			throw PermissionDeniedEx("You do not have permission to modify this user.")
		}

		return userRepository.save(user.applyPatch(patch))
			.dto()
			.also {
				jwtFilterService.blacklistOldTokensForSub(jwt.subject)
			}
	}
}
