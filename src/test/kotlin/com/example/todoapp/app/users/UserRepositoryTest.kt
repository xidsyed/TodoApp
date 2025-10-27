package com.example.todoapp.app.users

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.users.entity.UserEntity
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*

@SpringBootTest
class UserRepositoryTest @Autowired constructor(
	private val repo: UserRepository,
	private val dbClient: DatabaseClient
) {
	val userId1: UUID = UUID.randomUUID()
	val userId2: UUID = UUID.randomUUID()

	@AfterTest
	fun cleanup(): Unit = runBlocking {
		dbClient.sql("TRUNCATE TABLE user_profiles CASCADE").then().block()
	}


	@Test
	fun `create and get user`(): Unit = runBlocking {
		val user = UserEntity(
			userId = userId1,
			createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
			updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
			displayName = "test user",
			role = NewzroomRoleEntity.WRITER,
			email = "test1@example.com"

		)
		val savedUser = repo.save(user)
		assertNotNull(savedUser.userId)
		val foundUser = repo.findById(savedUser.userId)
		assertEquals(savedUser, foundUser)
	}

	@Test
	fun `create and fetch many users`(): Unit = runBlocking {
		val userList = listOf(
			UserEntity(
				userId = userId1,
				displayName = "test user 1",
				role = NewzroomRoleEntity.WRITER,
				email = "test1@example.com"
			), UserEntity(
				userId = userId2,
				displayName = "test user 2",
				role = NewzroomRoleEntity.WRITER,
				email = "test2@example.com"

			)
		)
		val savedList = repo.saveAll(userList).toList()
		assertContentEquals(userList, savedList, "the saved list and fetched list of entities must be identical")
	}

	@Test
	fun `findByEmail returns null if none found`(): Unit = runBlocking {
		val foundUser = repo.findByEmail("nonexistent@example.com")
		assertNull(foundUser)
	}

	@Test
	fun `findByEmail returns user if found`(): Unit = runBlocking {
		val user = UserEntity(
			userId = userId1,
			createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
			updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
			displayName = "test user",
			role = NewzroomRoleEntity.WRITER,
			email = "test1@example.com"
		)
		repo.save(user)
		val foundUser = repo.findByEmail(user.email)
		assertEquals(user, foundUser)
	}
}