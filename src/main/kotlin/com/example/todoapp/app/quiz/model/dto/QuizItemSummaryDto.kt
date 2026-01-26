package com.example.todoapp.app.quiz.model.dto

data class QuizItemSummaryDto(
	val quiz: QuizDto,
	val items: List<ItemSummaryDto>
)