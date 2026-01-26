package com.example.todoapp.app.quiz.model.dto

import java.util.*

data class ItemSummaryDto(
	val id: UUID,
	val title: String?,
	val question: String,
	val imageId: String?,
	val version: Int,
)