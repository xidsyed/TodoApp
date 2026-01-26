package com.example.todoapp.app.users

import com.example.todoapp.app.auth.jwt_filter.JwtFilterService
import com.example.todoapp.app.auth.roles.annotations.RequireAdmin
import com.example.todoapp.app.users.mapper.internalDto
import com.example.todoapp.app.users.model.*
import com.example.todoapp.common.util.err
import kotlinx.coroutines.flow.*
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@EnableReactiveMethodSecurity
@RequireAdmin
@Validated
@RequestMapping("/internal/users")
class UserInternalController(
	private val userService: UserService,
	private val jwtFilterService: JwtFilterService
) {

	@GetMapping
	suspend fun getAllUsersInternal(): Flow<UserInternalDto> {
		return userService.getAllUsers().map { it.internalDto() }
	}

	@GetMapping("/search")
	suspend fun searchUsersByNameOrEmail(@RequestParam query: String): Flow<UserInternalDto> {
		return userService.findUserByNameOrEmail(query).map { it.internalDto() }
	}

	@GetMapping("/{id}")
	suspend fun getUserInternal(@PathVariable id: UUID): UserInternalDto {
		return userService.getUser(id)?.internalDto() ?: throw err(NOT_FOUND, "User Not Found")
	}

	@PatchMapping("/{id}")
	suspend fun patchUser(
		@PathVariable id: UUID,
		@RequestBody patch: InternalPatchUserRequest
	): UserInternalDto {
		val user = userService.getUser(id) ?: throw err(NOT_FOUND, "User Not Found")
		val patchedUser = user.applyPatch(patch)
		return userService.saveUser(patchedUser).internalDto().also {
			jwtFilterService.blacklistOldTokensForSub(user.id.toString())
		}
	}
}