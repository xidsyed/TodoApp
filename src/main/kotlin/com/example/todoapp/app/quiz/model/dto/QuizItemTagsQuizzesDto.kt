package com.example.todoapp.app.quiz.model.dto

data class QuizItemTagsQuizzesDto(
	val quiz: QuizDto, val items: List<ItemTagsQuizzesDto>
)