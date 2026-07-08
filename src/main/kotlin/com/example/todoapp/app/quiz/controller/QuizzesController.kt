package com.example.todoapp.app.quiz.controller

import com.example.todoapp.app.quiz.domain.service.QuizService
import com.example.todoapp.app.quiz.model.dto.QuizItemTagsDto
import com.example.todoapp.common.exception.InvalidRequestEx
import com.github.michaelbull.result.getOrThrow
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(QuizzesController.QUIZ_PATH)
class QuizzesController(
	private val quizService: QuizService
) {
	companion object {
		const val QUIZ_PATH = "/quizzes"
	}

	@PutMapping("/{quizId}")
	suspend fun updateQuiz(
		@PathVariable quizId: UUID,
		@RequestBody request: QuizItemTagsDto
	): QuizItemTagsDto {
		if (quizId != request.quiz.id) {
			throw InvalidRequestEx("Quiz ID in path does not match ID in request body")
		}
		return quizService.upsertQuiz(request).getOrThrow()
	}
}