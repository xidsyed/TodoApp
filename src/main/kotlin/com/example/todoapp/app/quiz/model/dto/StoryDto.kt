package com.example.todoapp.app.quiz.model.dto

data class StoryDto(
	val parts: List<StoryPartDto>
) {
	data class StoryPartDto(
		val imageId: String?,
		val imageCaption: String? = null,
		val brief: String,
	)
}