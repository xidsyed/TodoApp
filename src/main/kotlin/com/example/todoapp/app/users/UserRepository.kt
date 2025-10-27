package com.example.todoapp.app.users

import com.example.todoapp.app.users.entity.UserEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CoroutineCrudRepository<UserEntity, UUID> {
	@Query("SELECT * from user_profiles WHERE email = :email LIMIT 1")
	suspend fun findByEmail(email:String) : UserEntity?
}