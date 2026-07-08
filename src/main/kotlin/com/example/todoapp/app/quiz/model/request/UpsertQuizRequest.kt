package com.example.todoapp.app.quiz.model.request

import com.example.todoapp.app.quiz.model.dto.StoryDto
import java.util.*

data class UpsertQuizRequest(
	val id: UUID? ,					// if id is null -> Insert Quiz
	val title: String,
	val description: String? ,
	val channelId: UUID,
	val authorId: UUID,
	val version: Int?,
	val items : List<Item>
) {
	data class Item(
		val id: UUID?, 				// if id is null -> Insert Quiz Item
		val title: String?,
		val description: String?,
		val imageId: String?,
		val question: String,
		val options: List<String>,
		val key: Short,
		val story: StoryDto?,
		val version: Int?,
		val tags : List<String>
	)
}

