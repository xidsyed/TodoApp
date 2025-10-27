package com.example.todoapp.app.invitation.service

import com.example.todoapp.app.invitation.InvitationRepository
import com.example.todoapp.app.invitation.entity.InvitationEntity
import com.example.todoapp.app.invitation.mapper.dto
import com.example.todoapp.app.invitation.model.InvitationDto
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.exception.UserNotFoundEx
import com.example.todoapp.app.users.mapper.dto
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Component

@Component
class InvitationService(private val invitationRepo: InvitationRepository, private val userRepo: UserRepository) {
	fun getAllInvitations(): Flow<InvitationDto> = invitationRepo.findAll().map { enrichedDtoFromEntity(it) }

	private suspend fun enrichedDtoFromEntity(entity: InvitationEntity): InvitationDto = entity.run {
		coroutineScope {
			val assignor = async { userRepo.findById(entity.assignor) ?: throw UserNotFoundEx(assignor) }
			val assignee = async {
				assignee?.let { userRepo.findById(it) ?: throw UserNotFoundEx(assignee) }
			}
			dto(assignor.await().dto(), assignee.await()?.dto())
		}
	}

}