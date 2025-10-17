package com.example.todoapp.app.invitation

import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.common.data.entity.NewzroomRoleEntity
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*
import kotlin.test.Test

@SpringBootTest
class InvitationRepositoryTest @Autowired constructor(
	private val repo: InvitationRepository,
	private val dbClient: DatabaseClient

) {
	private val userId = UUID.fromString("e8935160-596f-48a1-a654-e10c5922e8c2")
	private val userId2 = userId

	@AfterEach
	fun cleanup(): Unit = runBlocking {
		dbClient.sql("TRUNCATE TABLE invitation CASCADE").then().block()
	}



	@Test
	fun `create and get invitation`(): Unit = runBlocking {
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			createdBy = userId,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS),
			revokedAt = null,
			assignedTo = null,
		)
		val savedInvite = repo.save(invite)
		assertNotNull(savedInvite.id)
		val foundInvite = repo.findById(savedInvite.id!!)
		assertEquals(savedInvite, foundInvite)
	}

	@Test
	fun `create and fetch many invitations`() : Unit = runBlocking {
		val savedList = buildList<InvitationEntity>(10) {
			repeat(10){ index ->
				addLast(
					InvitationEntity(
						id = null,
						email = "email$index@email.com",
						createdBy = userId,
						role = NewzroomRoleEntity.WRITER,
						eat = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS),
						revokedAt = null,
						assignedTo = null,
					)
				)
			}
		}
		val fetchedList = repo.saveAll(savedList).toList()
		assertContentEquals(savedList, fetchedList, "the saved list and fetched list of entities must be identical")
	}

	@Test
	fun `revoked invitations are identified as revoked`(): Unit = runBlocking {
		val revocation = Instant.now().truncatedTo(ChronoUnit.MICROS)
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			createdBy = userId,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS),
			revokedAt = revocation,
			assignedTo = null,
		)
		val savedInvite = repo.save(invite)
		assertNotNull(savedInvite.revokedAt)
		assertEquals(invite.revokedAt, revocation)
	}

	@Test
	fun `assigned invitations are identified as assigned`(): Unit = runBlocking {
		val assignedTo = userId2
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			createdBy = userId,
			role = NewzroomRoleEntity.WRITER,
			eat = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS),
			revokedAt = null,
			assignedTo = assignedTo,
		)
		val savedInvite = repo.save(invite)
		assertEquals(savedInvite.assignedTo , assignedTo)
	}

	@Test
	fun `expired invitations are identified as expired`(): Unit = runBlocking {
		val expiredAt = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS)
		val invite = InvitationEntity(
			id = null,
			email = "iwanttosignup@email.com",
			createdBy = userId,
			role = NewzroomRoleEntity.WRITER,
			eat = expiredAt,
			revokedAt = null,
			assignedTo = null,
		)
		val savedInvite = repo.save(invite)
		assertEquals(savedInvite.eat, expiredAt )
	}
}