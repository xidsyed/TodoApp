package com.example.todoapp.app.users

import com.example.todoapp.NewzDBIntegrationTest
import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.util.logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*

class UserRepositoryTest @Autowired constructor(
	private val repo: UserRepository,
	private val dbClient: DatabaseClient
) : NewzDBIntegrationTest(){
	val userId1: UUID = UUID.randomUUID()
	val userId2: UUID = UUID.randomUUID()

	private val log = logger()

	@AfterTest
	fun cleanup(): Unit = runBlocking {
		repo.deleteAllById(listOf(userId1, userId2))
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
		).apply { isNewRecord = true }
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
		).apply { isNewRecord = true }

		repo.save(user)
		val foundUser = repo.findByEmail(user.email)
		assertEquals(user, foundUser)
	}

	@Test
	fun `findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase returns valid search results`(): Unit =
		runBlocking {
			val user1 = UserEntity(
				userId = userId1,
				displayName = "test1 name",
				role = NewzroomRoleEntity.WRITER,
				email = "test1@example.com"
			)
			val user2 = UserEntity(
				userId = userId2,
				displayName = "test2 name",
				role = NewzroomRoleEntity.WRITER,
				email = "test2@example.com"
			)

			val savedList = repo.saveAll(listOf(user1, user2)).toList()
			val testList =
				repo.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase("test", "test").toList()

			assertEqualIds(savedList, testList)

			val test1List =
				repo.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase("test1", "test1").toList()
			assertEqualIds(listOf(user1), test1List)

			val test2List =
				repo.findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase("test2", "test2").toList()
			assertEqualIds(listOf(user2), test2List)
		}
}

fun assertEqualIds(first: UserEntity, second: UserEntity) {
	assertEquals(first.userId, second.userId)
}

fun assertEqualIds(first: List<UserEntity>, second: List<UserEntity>) {
	first.asFlow().zip(second.asFlow()) { f, s ->
		assertEqualIds(f, s)
	}
}