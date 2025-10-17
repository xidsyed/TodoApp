package com.example.todoapp.app.invitation.controller

import com.example.todoapp.app.invitation.*
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.mapper.dto
import com.example.todoapp.app.invitation.model.*
import com.example.todoapp.app.users.*
import com.example.todoapp.app.users.mapper.dto
import com.example.todoapp.common.mapper.*
import jakarta.validation.Valid
import kotlinx.coroutines.*
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/invitation")
@Validated
class InvitationController(
	private val userRepo: UserRepository,
	private val invitationRepo: InvitationRepository,
) {

	@PostMapping("")
	suspend fun createInvitation(
		@Valid @RequestBody request: CreateInvitationRequest,
		@AuthenticationPrincipal jwt: Jwt
	): ResponseEntity<InvitationDto> {
		val userId = UUID.fromString(jwt.subject)
		val invitationEntity = invitationRepo.save(
			InvitationEntity(
				email = request.email, eat = request.expiresAt, role = request.role.entity(), createdBy = userId
			)
		)
		val response = enrichedDtoFromEntity(invitationEntity)
		return ResponseEntity.status(HttpStatus.CREATED).body(response)
	}

	// update invitation
	@PatchMapping("/{id}")
	suspend fun updateInvitation(
		@PathVariable id: UUID,
		@Valid @RequestBody request: UpdateInvitationRequest,
		@AuthenticationPrincipal jwt: Jwt
	): ResponseEntity<InvitationDto> {
		val userId = UUID.fromString(jwt.subject)

		val existingInvitationEntity = invitationRepo.findById(id) ?: throw invitationNotFound(id.toString())
		existingInvitationEntity.apply {
			if (revokedAt != null) throw ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"Invitation has already been revoked"
			)
			if (assignedTo != null) throw ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"Invitation has already been assigned"
			)
			if (createdBy != userId) throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		val updatedInvitation = existingInvitationEntity.copy(
			role = request.role?.entity() ?: existingInvitationEntity.role,
			eat = request.expiresAt ?: existingInvitationEntity.eat,
			email = request.email ?: existingInvitationEntity.email,
			revokedAt = request.revokedAt,
		)

		val savedInvitation = invitationRepo.save(updatedInvitation)
		val response = enrichedDtoFromEntity(savedInvitation)
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response)
	}


	private suspend fun enrichedDtoFromEntity(entity: InvitationEntity): InvitationDto = coroutineScope {
		entity.run{
			val creator = async { userRepo.findById(entity.createdBy) ?: throw userNotFound(createdBy.toString()) }
			val assigned = async {
				assignedTo?.let { userRepo.findById(it) ?: throw userNotFound(assignedTo.toString()) }
			}
			dto(creator.await().dto(), assigned.await()?.dto())
		}
	}

}