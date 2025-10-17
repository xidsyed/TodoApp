package com.example.todoapp.app.users

import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.data.entity.NewzroomRoleEntity
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
class UserRepositoryTest @Autowired constructor(
	private val repo: UserRepository,
	private val dbClient: DatabaseClient
) {
	val userId1: UUID = UUID.fromString("e8935160-596f-48a1-a654-e10c5922e8c2")
	val userId2: UUID = UUID.fromString("1cff6b2b-720f-4a3c-8d34-673af631a2b5")!!

	@AfterEach
	fun cleanup(): Unit = runBlocking {
		dbClient.sql("TRUNCATE TABLE newzroom_user_profiles CASCADE").then().block()
	}


	@Test
	fun `create and get user`(): Unit = runBlocking {
		val user = UserEntity(
			userId = userId1,
			createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
			updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
			displayName = "test user",
			role = NewzroomRoleEntity.WRITER
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
				createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
				updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
				displayName = "test user 1",
				role = NewzroomRoleEntity.WRITER
			), UserEntity(
				userId = userId2,
				createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
				updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS),
				displayName = "test user 2",
				role = NewzroomRoleEntity.WRITER
			)
		)
		val savedList = repo.saveAll(userList).toList()
		assertContentEquals(userList, savedList, "the saved list and fetched list of entities must be identical")
	}
}