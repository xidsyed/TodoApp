package com.example.todoapp.app.quiz.model.dto

// Item with tags (wrapper)
data class ItemTagsDto(
	val item: ItemDto,
	val tags: List<String>
)
