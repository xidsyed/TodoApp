package com.example.todoapp.app.invitation

import com.example.todoapp.AbstractIntegrationTest
import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*

class InvitationRepositoryTest @Autowired constructor(
	private val repo: InvitationRepository,
	private val userRepository: UserRepository,
) : AbstractIntegrationTest() {
	private val userId1 = UUID.randomUUID()
	private val userId2 = UUID.randomUUID()

	private val logger = LoggerFactory.getLogger(InvitationRepositoryTest::class.java)


	@BeforeEach
	fun setupTests() {
		runBlocking {
			userRepository.saveAll(
				listOf(
					UserEntity(
						userId = userId1,
						displayName = "user 1",
						profilePic = "https://picsum.photos/id/1/200/300.jpg",
						role = NewzroomRoleEntity.ADMIN,
						email = "test1@example.com"
					),
					UserEntity(
						userId = userId2,
						displayName = "user 2",
						profilePic = "https://picsum.photos/id/12/200/300.jpg",
						role = NewzroomRoleEntity.WRITER,
						email = "test2@example.com"
					)
				)
			).collect()
		}
	}


	@AfterEach
	fun cleanup(): Unit = runBlocking {
		// The database is managed by Testcontainers and Flyway.
		// For test isolation, we should clean up data inserted by this test.
		// Truncating is a simple way to do this.
		databaseClient.sql("TRUNCATE TABLE invitations CASCADE").then().block()
		userRepository.deleteAllById(listOf(userId1, userId2))
	}

	@Test
	fun `create and get invitation`(): Unit = runBlocking {
		val invite = InvitationEntity(
			email = "iwanttosignup@email.com",
			assignor = userId1,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().plus(1, ChronoUnit.DAYS),
			createdAt = Instant.now(),
			assignee = null,
		)
		val savedInvite = repo.save(invite)
		assertNotNull(savedInvite.id)
		val foundInvite = repo.findById(savedInvite.id!!)
		assertEquals(savedInvite, foundInvite)
	}

	@Test
	fun `create and fetch many invitations`(): Unit = runBlocking {
		val savedList = buildList(10) {
			repeat(10) { index ->
				addLast(
					InvitationEntity(
						id = null,
						email = "email$index@email.com",
						assignor = userId1,
						role = NewzroomRoleEntity.WRITER,
						eat = Instant.now().plus(1, ChronoUnit.DAYS),
						assignee = null,
					)
				)
			}
		}
		val fetchedList = repo.saveAll(savedList).toList()
		assertContentEquals(savedList, fetchedList, "the saved list and fetched list of entities must be identical")
	}

	@Test
	fun `assigned invitations are identified as assigned`(): Unit = runBlocking {
		val assignee = userId2
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			assignor = userId1,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().plus(1, ChronoUnit.DAYS),
			assignee = assignee,
		)
		val savedInvite = repo.save(invite)
		assertEquals(savedInvite.assignee, assignee)
	}

	@Test
	fun `expired invitations are identified as expired`(): Unit = runBlocking {
		val expiredAt = Instant.now().plus(1, ChronoUnit.DAYS)
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			assignor = userId1,
			role = NewzroomRoleEntity.WRITER,
			eat = expiredAt,
			assignee = null,
		)
		val savedInvite = repo.save(invite)
		assertEquals(savedInvite.eat, expiredAt)
	}

	@Test
	fun `cannot add invitation with eat less than or equal to current time`(): Unit = runBlocking {
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			assignor = userId1,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().minus(1, ChronoUnit.DAYS),
			assignee = null,
		)
		assertFailsWith<DataIntegrityViolationException> {
			repo.save(invite)
		}
	}

	@Test
	fun `cannot add two invitations with the same email`(): Unit = runBlocking {
		val invite1 = InvitationEntity(
			id = null,
			email = "duplicate@email.com",
			assignor = userId1,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().plus(1, ChronoUnit.DAYS),
			assignee = null,
		)
		repo.save(invite1)

		val invite2 = InvitationEntity(
			id = null,
			email = "duplicate@email.com",
			assignor = userId1,
			role = NewzroomRoleEntity.ADMIN,
			eat = Instant.now().plus(2, ChronoUnit.DAYS),
			assignee = null,
		)
		assertFailsWith<DataIntegrityViolationException> {
			repo.save(invite2)
		}
	}
}