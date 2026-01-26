package com.example.todoapp.app.quiz.model.view

data class StoryView(
	val parts: List<StoryPartView>
) {
	data class StoryPartView(
		val imageId: String?,
		val imageCaption: String? = null,
		val brief: String,
	)
}