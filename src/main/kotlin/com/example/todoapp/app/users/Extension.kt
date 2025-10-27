package com.example.todoapp.app.users

import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.app.users.exception.UserNotFoundEx
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

suspend fun UserRepository.userFromJwt(jwt: Jwt): UserEntity {
	val uuid = UUID.fromString(jwt.subject)
	return findById(uuid) ?: throw UserNotFoundEx(uuid)
}

