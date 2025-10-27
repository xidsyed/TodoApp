package com.example.todoapp.app.invitation.controller

import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole
import com.example.todoapp.app.auth.roles.data.model.NewzroomRole.ADMIN
import com.example.todoapp.app.invitation.InvitationRepository
import com.example.todoapp.app.invitation.controller.InvitationController.Companion.INVITATION_PATH
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.model.*
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.util.logger
import com.example.todoapp.test.WithMockJwt
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

private const val TEST_USER_ID_STRING = "f3a6c539-d7a7-4432-8a23-6374106fab27"
private val TEST_USER_ID = UUID.fromString(TEST_USER_ID_STRING)

@SpringBootTest
@AutoConfigureWebTestClient
class InvitationControllerTest(
	@Autowired private val client: WebTestClient,
	@Autowired private val userRepo: UserRepository,
	@Autowired private val invitationRepo: InvitationRepository
) {

	private val log = logger()

	@BeforeEach
	fun setup(): Unit = runBlocking {
		userRepo.save(
			UserEntity(
				userId = TEST_USER_ID,
				displayName = "Test User",
				role = NewzroomRoleEntity.ADMIN,
				email = "testuser@example.com"
			)
		)
	}

	@AfterEach
	fun cleanup() = runBlocking {
		invitationRepo.deleteAll()
		userRepo.deleteAll()
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should create an invitation`() {
		client.post().uri(INVITATION_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(
				CreateInvitationRequest(
					email = "invitationcontrollertest@example.com",
					expiresAt = Instant.now().plus(2, ChronoUnit.HOURS),
					role = NewzroomRole.WRITER
				)
			)
			.exchange()
			.expectStatus().isCreated
			.expectBody()
			.jsonPath("$.email").isEqualTo("invitationcontrollertest@example.com")
			.jsonPath("$.role").isEqualTo(NewzroomRole.WRITER.value)
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should return conflict when creating an invitation for an existing user`(): Unit = runBlocking {
		userRepo.save(
			UserEntity(
				userId = UUID.randomUUID(),
				displayName = "Existing User",
				role = NewzroomRoleEntity.WRITER,
				email = "invitationcontrollertest@example.com"
			)
		)

		client.post().uri(INVITATION_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(
				CreateInvitationRequest(
					email = "invitationcontrollertest@example.com",
					expiresAt = Instant.now().plus(2, ChronoUnit.HOURS),
					role = NewzroomRole.WRITER
				)
			)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.CONFLICT)
			.expectBody()
			.consumeWith { println("Response body: ${String(it.responseBody!!)}") }
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should update an invitation`() {
		val invitation = runBlocking {
			invitationRepo.save(
				InvitationEntity(
					email = "invitationcontrollertest@example.com",
					eat = Instant.now().plus(2, ChronoUnit.HOURS),
					role = NewzroomRoleEntity.WRITER,
					assignor = TEST_USER_ID
				)
			)
		}

		client.patch().uri("$INVITATION_PATH/${invitation.id}")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(
				PatchInvitationRequest(
					email = "new-email@example.com",
					role = ADMIN
				)
			)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.email").isEqualTo("new-email@example.com")
			.jsonPath("$.role").isEqualTo(ADMIN.value)

	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should delete an invitation`() {
		val invitation = runBlocking {
			invitationRepo.save(
				InvitationEntity(
					email = "invitationcontrollertest@example.com",
					eat = Instant.now().plus(2, ChronoUnit.HOURS),
					role = NewzroomRoleEntity.WRITER,
					assignor = TEST_USER_ID
				)
			)
		}

		client.delete().uri("$INVITATION_PATH/${invitation.id}")
			.exchange()
			.expectStatus().isNoContent
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should get an invitation by id`() {
		val invitation = runBlocking {
			invitationRepo.save(
				InvitationEntity(
					email = "invitationcontrollertest@example.com",
					eat = Instant.now().plus(2, ChronoUnit.HOURS),
					role = NewzroomRoleEntity.WRITER,
					assignor = TEST_USER_ID
				)
			)
		}

		client.get().uri("$INVITATION_PATH/${invitation.id}")
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.id").isEqualTo(invitation.id.toString())
			.jsonPath("$.email").isEqualTo("invitationcontrollertest@example.com")
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should get all invitations`(): Unit = runBlocking {
		invitationRepo.save(
			InvitationEntity(
				email = "test1@example.com",
				eat = Instant.now().plus(2, ChronoUnit.HOURS),
				role = NewzroomRoleEntity.WRITER,
				assignor = TEST_USER_ID
			)
		)
		invitationRepo.save(
			InvitationEntity(
				email = "test2@example.com",
				eat = Instant.now().plus(2, ChronoUnit.HOURS),
				role = NewzroomRoleEntity.ADMIN,
				assignor = TEST_USER_ID
			)
		)

		client.get().uri("$INVITATION_PATH/all")
			.exchange()
			.expectStatus().isOk
			.expectBodyList(InvitationDto::class.java)
			.hasSize(2)
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should return bad request when updating an assigned invitation`(): Unit = runBlocking {
		val assignee = userRepo.save(
			UserEntity(
				userId = UUID.randomUUID(),
				email = "assignee@example.com",
				displayName = "assignee",
				profilePic = "",
				role = NewzroomRoleEntity.WRITER
			)
		)
		val invitationEntity = invitationRepo.save(
			InvitationEntity(
				email = "invitationcontrollertest@example.com",
				eat = Instant.now().plus(2, ChronoUnit.HOURS),
				role = NewzroomRoleEntity.WRITER,
				assignor = TEST_USER_ID,
				assignee = assignee.id// Make it assigned
			)
		)

		client.patch().uri("$INVITATION_PATH/${invitationEntity.id}")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(PatchInvitationRequest(email = "new-email@example.com"))
			.exchange()
			.expectStatus().isBadRequest
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should return bad request when updating an expired invitation`(): Unit = runBlocking {
		val invitationEntity = invitationRepo.save(
			InvitationEntity(
				email = "invitationcontrollertest@example.com",
				eat = Instant.now().minus(1, ChronoUnit.HOURS), // Expired
				role = NewzroomRoleEntity.WRITER,
				assignor = TEST_USER_ID
			)
		)

		client.patch().uri("$INVITATION_PATH/${invitationEntity.id}")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(PatchInvitationRequest(email = "new-email@example.com"))
			.exchange()
			.expectStatus().isBadRequest
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should return forbidden when deleting an assigned invitation`(): Unit = runBlocking {
		val assignee = userRepo.save(
			UserEntity(
				userId = UUID.randomUUID(),
				email = "assignee@example.com",
				displayName = "assignee",
				profilePic = "",
				role = NewzroomRoleEntity.WRITER
			)
		)

		val invitationEntity = invitationRepo.save(
			InvitationEntity(
				email = "invitationcontrollertest@example.com",
				eat = Instant.now().plus(2, ChronoUnit.HOURS),
				role = NewzroomRoleEntity.WRITER,
				assignor = TEST_USER_ID,
				assignee = assignee.id
			)
		)

		client.delete().uri("$INVITATION_PATH/${invitationEntity.id}")
			.exchange()
			.expectStatus().isForbidden
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should return bad request when creating an invitation with expiration less than 1 hour`() {
		client.post().uri(INVITATION_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(
				CreateInvitationRequest(
					email = "invitationcontrollertest@example.com",
					expiresAt = Instant.now(),
					role = NewzroomRole.WRITER
				)
			)
			.exchange()
			.expectStatus().isBadRequest
	}

	@Test
	@WithMockJwt(subject = TEST_USER_ID_STRING, role = ADMIN)
	fun `should return bad request when creating an invitation with expiration more than 24 hours`() {
		client.post().uri(INVITATION_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(
				CreateInvitationRequest(
					email = "invitationcontrollertest@example.com",
					expiresAt = Instant.now().plus(25, ChronoUnit.HOURS),
					role = NewzroomRole.WRITER
				)
			)
			.exchange()
			.expectStatus().isBadRequest
	}
}
