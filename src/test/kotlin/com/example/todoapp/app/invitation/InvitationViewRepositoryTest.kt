package com.example.todoapp.app.invitation

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.mapper.dto
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.app.users.mapper.dto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.*

@SpringBootTest
class InvitationViewRepositoryTest @Autowired constructor(
	private val invitationRepo: InvitationRepository,
	private val viewRepo: InvitationViewRepository,
	private val userRepo: UserRepository,
	private val dbClient: DatabaseClient
) {


	private val userId1 = UUID.randomUUID()
	private val userId2 = UUID.randomUUID()

	private val logger = LoggerFactory.getLogger(InvitationViewRepositoryTest::class.java)

	@BeforeTest
	fun setupTests() {
		runBlocking {
			val user1 = UserEntity(
				userId = userId1,
				displayName = "user 1",
				profilePic = "https://picsum.photos/id/1/200/300.jpg",
				role = NewzroomRoleEntity.ADMIN,
				email = "test1@example.com"
			)

			val user2 = UserEntity(
				userId = userId2,
				displayName = "user 2",
				profilePic = "https://picsum.photos/id/12/200/300.jpg",
				role = NewzroomRoleEntity.WRITER,
				email = "test2@example.com"
			)


			userRepo.saveAll(listOf(user1, user2)).collect()

			val invitation1 = InvitationEntity(
				email = "test1@example.com",
				assignor = user1.id,
				role = NewzroomRoleEntity.WRITER,
				eat = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS),
				assignee = user2.id
			)
			val invitation2 = InvitationEntity(
				email = "test2@example.com",
				assignor = user2.id,
				role = NewzroomRoleEntity.ADMIN,
				eat = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MICROS),
				assignee = null
			)

			invitationRepo.saveAll(listOf(invitation1, invitation2)).toList()

		}

	}

	@AfterTest
	fun cleanup(): Unit = runBlocking {
		dbClient.sql("TRUNCATE TABLE invitations CASCADE").then().block()
		userRepo.deleteAllById(listOf(userId1, userId2))
	}

	@Test
	fun `InvitationView returned by findViewAll can be mapped to InvitationDto`(): Unit = runBlocking {

		val user1 = userRepo.findById(userId1)
		val user2 = userRepo.findById(userId2)
		if (user1 == null || user2 == null) throw IllegalStateException("User could not be inserted")

		val invitation1 = invitationRepo.findAll().first { it.email == "test1@example.com" }
		val invitation2 = invitationRepo.findAll().first { it.email == "test2@example.com" }

		val invitationViews = viewRepo.findViewAll().toList()

		assertEquals(2, invitationViews.size, "Should return two invitation views")

		val view1 = invitationViews.first { it.email == "test1@example.com" }
		val view2 = invitationViews.first { it.email == "test2@example.com" }


		assertEquals(
			invitation1.dto(assignor = user1.dto(), assignee = user2.dto()),
			view1.dto()
		)
		assertEquals(
			invitation2.dto(assignor = user2.dto(), assignee = null),
			view2.dto()
		)
	}

	@Test
	fun `InvitationView returned by findView can be mapped to InvitationDto`(): Unit = runBlocking {

		val user1 = userRepo.findById(userId1)
		val user2 = userRepo.findById(userId2)
		if (user1 == null || user2 == null) throw IllegalStateException("User could not be inserted")

		val invitation1 = invitationRepo.findAll().first { it.email == "test1@example.com" }
		val invitation2 = invitationRepo.findAll().first { it.email == "test2@example.com" }

		val view1 = viewRepo.findViewById(invitation1.id!!) ?: throw IllegalStateException("View could not be inserted")
		val view2 = viewRepo.findViewById(invitation2.id!!) ?: throw IllegalStateException("View could not be inserted")

		assertEquals(
			invitation1.dto(assignor = user1.dto(), assignee = user2.dto()),
			view1.dto()
		)
		assertEquals(
			invitation2.dto(assignor = user2.dto(), assignee = null),
			view2.dto()
		)

	}
}