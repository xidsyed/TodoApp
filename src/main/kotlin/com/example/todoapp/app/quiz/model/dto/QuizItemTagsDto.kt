package com.example.todoapp.app.quiz.model.dto

data class QuizItemTagsDto(
	val quiz: QuizDto,
	val items: List<ItemTagsDto>
)
