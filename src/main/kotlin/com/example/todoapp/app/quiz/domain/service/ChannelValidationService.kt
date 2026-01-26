package com.example.todoapp.app.quiz.domain.service

import com.example.todoapp.app.quiz.domain.validation.newzleChannelQuizValidator
import com.example.todoapp.app.quiz.model.domain.ReservedChannel
import com.example.todoapp.app.quiz.model.dto.QuizItemTagsDto
import com.example.todoapp.app.quiz.repository.ChannelRepository
import com.example.todoapp.common.exception.*
import com.github.michaelbull.result.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.boot.*
import org.springframework.stereotype.Component
import java.util.*


/**
 * This service is responsible for validating quizzes being published to a channel as per the channel constraints
 * */
@Component
class ChannelValidationService(private val channelRepo: ChannelRepository) {

	val reservedChannelById = mutableMapOf<UUID, ReservedChannel>()

	/** Returns Unit if validation is successful
	 * @throws [DomainEx] on failure*/
	suspend fun validateRequest(request: QuizItemTagsDto): Result<Unit, UnprocessableEx> {
		return when (reservedChannelById[request.quiz.channelId]) {
			ReservedChannel.DRAFT -> draftValidation(request)
			ReservedChannel.NEWZLE -> newzleValidation(request)
			else -> {
				if (!channelRepo.existsById(request.quiz.channelId)) {
					Err(UnprocessableEx("Invalid channel id : ${request.quiz.channelId}"))
				} else Ok(Unit)
			}
		}
	}

	private fun draftValidation(request: QuizItemTagsDto): Result<Unit, UnprocessableEx> {
		return Ok(Unit)
	}

	private fun newzleValidation(request: QuizItemTagsDto): Result<Unit, UnprocessableEx> {
		val result = newzleChannelQuizValidator.validate(request)
		return if (!result.isValid) {
			val fieldErrors = result.errors.map {
				FieldError(it.dataPath, it.message)
			}
			Err(UnprocessableEx(fieldErrors = fieldErrors))
		} else Ok(Unit)
	}

	suspend fun initialize() {
		val channelEntities = channelRepo.findAll().toList().associateBy { it.name }
		ReservedChannel.entries.forEach { channelEnum ->
			val channelEntity = channelEntities[channelEnum.name.lowercase()]
				?: throw InternalServerEx("Failed to setup channel validation. Channel ${channelEnum.name} not found in database")
			reservedChannelById[channelEntity.id!!] = channelEnum
		}
	}
}


@Component
class ChannelValidationInitializer(
	private val service: ChannelValidationService
) : ApplicationRunner {
	override fun run(args: ApplicationArguments) {
		runBlocking { service.initialize() }
	}
}