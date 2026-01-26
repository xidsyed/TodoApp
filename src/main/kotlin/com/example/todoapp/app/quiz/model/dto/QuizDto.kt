package com.example.todoapp.app.quiz.model.dto

import java.time.Instant
import java.util.*

data class QuizDto(
	val id: UUID,
	val title: String,
	val description: String?,
	val channelId: UUID,
	val authorId: UUID?,
	val createdAt: Instant,
	val publishedAt: Instant,
	val version: Int
)
