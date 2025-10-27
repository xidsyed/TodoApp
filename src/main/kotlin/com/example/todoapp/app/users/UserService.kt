package com.example.todoapp.app.users

import com.example.todoapp.app.users.entity.UserEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
	private val userRepository: UserRepository
) {

	suspend fun getAllUsers() = userRepository.findAll()

	suspend fun getUser(id : UUID) = userRepository.findById(id)

	suspend fun saveUser(user : UserEntity) = userRepository.save(user)

}