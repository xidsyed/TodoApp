package com.example.todoapp.app.quiz.model.dto

data class ItemTagsQuizzesDto(
	val item: ItemDto,
	val tags: List<String>,
	val quizzes: List<QuizDto>
)