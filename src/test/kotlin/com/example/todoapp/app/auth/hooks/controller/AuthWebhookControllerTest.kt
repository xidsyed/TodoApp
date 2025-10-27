package com.example.todoapp.app.auth.hooks.controller

import com.example.todoapp.app.auth.hooks.model.JwtPayload
import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.invitation.InvitationRepository
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.util.logger
import com.example.todoapp.core.webhook.*
import com.example.todoapp.core.webhook.Webhook.Companion.UNBRANDED_MSG_ID_KEY
import com.example.todoapp.core.webhook.Webhook.Companion.UNBRANDED_MSG_SIGNATURE_KEY
import com.example.todoapp.core.webhook.Webhook.Companion.UNBRANDED_MSG_TIMESTAMP_KEY
import com.example.todoapp.core.webhook.properties.WebhookSource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*
import kotlin.test.AfterTest

@SpringBootTest
@AutoConfigureWebTestClient
class AuthWebhookControllerTest @Autowired constructor(
	private val client: WebTestClient,
	private val userRepository: UserRepository,
	private val invitationRepository: InvitationRepository,
	private val objectMapper: ObjectMapper,
	webhookRegistry: WebhookRegistry,
) {

	private val webhook: Webhook = webhookRegistry[WebhookSource.SUPABASE]
	private val log = logger()

	companion object {
		private val ASSIGNOR_ID = UUID.randomUUID()
		private val ASSIGNEE_ID = UUID.randomUUID()
		private const val ASSIGNOR_EMAIL = "assignor@example.com"
		private const val ASSIGNEE_EMAIL = "assignee@example.com"

	}

	@AfterTest
	fun tearDown() = runBlocking {
		invitationRepository.deleteAll()
		userRepository.deleteAll()
	}

	@Test
	fun `beforeUserCreated should return 200 for valid invitation`() {
		seedUsersAndInvitation()
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/before_user_created")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isOk
	}

	@Test
	fun `beforeUserCreated should return 403 for no invitation`() {
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/before_user_created")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isForbidden
	}

	@Test
	fun `beforeUserCreated should return 403 for expired invitation`() {
		seedUsersAndInvitation { eat = Instant.now().minusSeconds(3600) }
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/before_user_created")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isForbidden
	}

	@Test
	fun `beforeUserCreated should return 403 for assigned invitation`() {
		seedUsersAndInvitation { assignInvitation = true }
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/before_user_created")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isForbidden
	}

	@Test
	fun `beforeUserCreated should return 403 for mismatched email`() {
		seedUsersAndInvitation { invitationEmail = "wrong@example.com" }
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/before_user_created")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isForbidden
	}

	@Test
	fun `beforeUserCreated should return 401 for invalid signature`() {
		val (payload, headers) = generateSignedPayload(defaultJwt)
		val invalidHeaders = headers.toSingleValueMap().mapValues { (key, value) ->
			if (key == UNBRANDED_MSG_SIGNATURE_KEY) "v1,invalid_signature_string" else value
		}

		client.post().uri("/auth/hooks/before_user_created")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.setAll(invalidHeaders) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isUnauthorized
	}

	@Test
	fun `customAccessToken should create user and return 200 for valid invitation`() {
		seedUsersAndInvitation()
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/custom_access_token")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isOk
			.expectBody()
			.jsonPath("$.claims.app_role").isEqualTo("writer")

		runBlocking {
			val user = userRepository.findById(ASSIGNEE_ID)
			assert(user != null) { "User not found" }
			assert(user?.role == NewzroomRoleEntity.WRITER) { "Role mismatch" }
		}
	}

	@Test
	fun `customAccessToken should return 200 with user roles for existing user`() {
		seedUsersAndInvitation { assignInvitation = true }
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/custom_access_token")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isOk
	}

	@Test
	fun `customAccessToken should return 403 for frozen user`() {
		seedUsersAndInvitation { freezeAssignee = true }
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/custom_access_token")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isForbidden
	}

	@Test
	fun `customAccessToken should return 403 for new user with no invitation`() {
		val (payload, headers) = generateSignedPayload(defaultJwt)

		client.post().uri("/auth/hooks/custom_access_token")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.addAll(headers) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isForbidden
	}


	@Test
	fun `customAccessToken should return 401 for invalid signature`() {
		val (payload, headers) = generateSignedPayload(defaultJwt)
		val invalidHeaders = headers.toSingleValueMap().mapValues { (key, value) ->
			if (key == UNBRANDED_MSG_SIGNATURE_KEY) "v1,invalid_signature_string" else value
		}

		client.post().uri("/auth/hooks/custom_access_token")
			.contentType(MediaType.APPLICATION_JSON)
			.headers { it.setAll(invalidHeaders) }
			.bodyValue(payload)
			.exchange()
			.expectStatus().isUnauthorized
	}

	private fun generateSignedPayload(payload: Any): Pair<String, HttpHeaders> {
		val payloadString = objectMapper.writeValueAsString(payload)
		val timestamp = Instant.now().epochSecond
		val messageId = UUID.randomUUID().toString()
		val signature = webhook.sign(messageId, timestamp, payloadString)
		val headers = HttpHeaders().apply {
			add(UNBRANDED_MSG_ID_KEY, messageId)
			add(UNBRANDED_MSG_TIMESTAMP_KEY, timestamp.toString())
			add(UNBRANDED_MSG_SIGNATURE_KEY, signature)
		}
		return Pair(payloadString, headers)
	}

	private fun seedUsersAndInvitation(configure: SeedConfig.() -> Unit = {}) = runBlocking {
		val cfg = SeedConfig().apply(configure)

		userRepository.save(
			UserEntity(
				userId = ASSIGNOR_ID,
				displayName = "Assignor",
				role = NewzroomRoleEntity.ADMIN,
				email = ASSIGNOR_EMAIL
			)
		)
		val assignee = if (cfg.assignInvitation || cfg.freezeAssignee) {
			userRepository.save(
				UserEntity(
					userId = ASSIGNEE_ID,
					displayName = "Assignee",
					role = NewzroomRoleEntity.WRITER,
					email = ASSIGNEE_EMAIL,
					freezeTill = if (cfg.freezeAssignee) Instant.now().plusSeconds(3600) else null,
					freezeCause = if (cfg.freezeAssignee) "Test Freeze" else null
				)
			)
		} else null
		invitationRepository.save(
			InvitationEntity(
				email = cfg.invitationEmail ?: ASSIGNEE_EMAIL,
				role = NewzroomRoleEntity.WRITER,
				eat = cfg.eat,
				assignor = ASSIGNOR_ID,
				assignee = assignee?.userId
			)
		)
	}


	class SeedConfig {
		var eat: Instant = Instant.now().plusSeconds(3600)
		var assignInvitation: Boolean = false
		var invitationEmail: String? = null
		var freezeAssignee: Boolean = false
	}

	private val defaultJwt: JwtPayload
		get() {
			val now = Instant.now().epochSecond.toInt()
			return JwtPayload(
				authenticationMethod = "oauth",
				userId = ASSIGNEE_ID.toString(),
				claims = JwtPayload.Claims(
					iss = "https://example.com",
					sub = ASSIGNEE_ID.toString(),
					aud = "authenticated",
					exp = now + 3600,
					iat = now,
					aal = "aal1",
					email = ASSIGNEE_EMAIL,
					phone = "",
					role = "authenticated",
					sessionId = UUID.randomUUID().toString(),
					isAnonymous = false,
					appMetadata = JwtPayload.Claims.AppMetadata(
						provider = "email",
						providers = listOf("email")
					),
					userMetadata = JwtPayload.Claims.UserMetadata(
						avatarUrl = "url",
						fullName = "Test User",
						picture = "url"
					)
				)
			)
		}

}
