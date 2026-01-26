package com.example.todoapp.app.quiz.model.dto

import java.time.Instant
import java.util.*

data class ItemDto(
	val id: UUID,
	val title: String?,
	val description: String?,
	val imageId: String?,
	val question: String,
	val options: List<String>,
	val key: Short,
	val story: StoryDto?,
	val createdAt: Instant,
	val version: Int,
	val quizItemRelationVersion: Int
)