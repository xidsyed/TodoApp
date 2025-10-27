package com.example.todoapp.app.invitation.controller

import com.example.todoapp.app.auth.roles.data.mapper.entity
import com.example.todoapp.app.invitation.*
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.mapper.dto
import com.example.todoapp.app.invitation.model.*
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.common.util.*
import jakarta.validation.Valid
import kotlinx.coroutines.flow.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*



@RestController
@RequestMapping(InvitationController.INVITATION_PATH)
@Validated
class InvitationController(
	private val userRepo: UserRepository,
	private val invitationRepo: InvitationRepository,
	private val invitationViewRepo: InvitationViewRepository
) {

	companion object {
		const val INVITATION_PATH = "/invitation"
	}

	private val log = logger()

	@PostMapping("")
	suspend fun createInvitation(
		@Valid @RequestBody request: CreateInvitationRequest,
		@AuthenticationPrincipal jwt: Jwt
	): ResponseEntity<InvitationDto> {
		val assignorUuid = UUID.fromString(jwt.subject)
		if (userRepo.findByEmail(request.email) != null) throw err(CONFLICT, "User already exists")
		if (!isValidExpiration(request.expiresAt)) throw err(
			BAD_REQUEST, "Expiration time must be between 1 and 24 hours from now"
		)

		val createdInvitation = invitationRepo.save(
			InvitationEntity(
				email = request.email,
				eat = request.expiresAt,
				role = request.role.entity(),
				assignor = assignorUuid
			)
		)
		val invitationView = invitationViewRepo.findViewById(createdInvitation.id!!)
		return res(CREATED, invitationView!!.dto())
	}

	@GetMapping("/{id}")
	suspend fun getInvitation(@PathVariable id: UUID): InvitationDto {
		return invitationViewRepo.findViewById(id)?.dto() ?: throw invitationNotFound(id.toString())
	}

	@GetMapping("/all")
	suspend fun getAllInvitations(): Flow<InvitationDto> {
		return invitationViewRepo.findViewAll().map { it.dto() }
	}

	@PatchMapping("/{id}")
	suspend fun patchInvitation(
		@PathVariable id: UUID,
		@Valid @RequestBody patch: PatchInvitationRequest,
	): ResponseEntity<InvitationDto> {

		val existingInvitation = invitationRepo.findById(id)?.apply {
			if (assignee != null) throw err(
				BAD_REQUEST,
				"Invitation has already been assigned"
			)
		} ?: throw invitationNotFound(id.toString())

		if (existingInvitation.eat <= Instant.now()) throw err(
			BAD_REQUEST,
			"Invitation has already expired"
		)

		val updatedInvitation = existingInvitation.applyPatch(patch)

		invitationRepo.save(updatedInvitation)
		val savedInvitation = invitationViewRepo.findViewById(id)?.dto() ?: throw invitationNotFound(id.toString())
		return res(OK, savedInvitation)
	}

	@DeleteMapping("/{id}")
	suspend fun deleteInvitation(
		@PathVariable id: UUID
	): ResponseEntity<Unit> {
		val invitation = invitationRepo.findById(id) ?: throw invitationNotFound(id.toString())
		if (invitation.assignee != null) throw err(
			FORBIDDEN,
			"Invitation has been assigned"
		)
		invitationRepo.deleteById(id)
		return res(NO_CONTENT)
	}

	private suspend fun isValidExpiration(eat: Instant): Boolean {
		return eat.isBefore(Instant.now().plus(Duration.ofHours(24))) &&
				eat.isAfter(Instant.now().plus(1, ChronoUnit.HOURS))
	}

}