package com.example.todoapp.app.quiz.model.dto

data class QuizItemDto(
	val quiz: QuizDto,
	val items: List<ItemDto>
)