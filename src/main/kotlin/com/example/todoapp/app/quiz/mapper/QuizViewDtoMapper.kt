package com.example.todoapp.app.quiz.mapper

import com.example.todoapp.app.quiz.model.dto.*
import com.example.todoapp.app.quiz.model.view.*

fun QuizItemView.toQuizDto(): QuizDto =
	QuizDto(
		id = quizId,
		title = quizTitle,
		description = quizDescription,
		channelId = quizChannelId,
		authorId = quizAuthorId,
		createdAt = quizCreatedAt,
		publishedAt = quizPublishedAt,
		version = quizVersion
	)

fun QuizItemTagsView.toQuizDto(): QuizDto =
	QuizDto(
		id = quizId,
		title = quizTitle,
		description = quizDescription,
		channelId = quizChannelId,
		authorId = quizAuthorId,
		createdAt = quizCreatedAt,
		publishedAt = quizPublishedAt,
		version = quizVersion
	)

fun QuizItemSummaryView.toQuizDto(): QuizDto =
	QuizDto(
		id = quizId,
		title = quizTitle,
		description = quizDescription,
		channelId = quizChannelId,
		authorId = quizAuthorId,
		createdAt = quizCreatedAt,
		publishedAt = quizPublishedAt,
		version = quizVersion
	)

fun QuizItemTagsQuizzesView.toQuizDto(): QuizDto =
	QuizDto(
		id = quizId,
		title = quizTitle,
		description = quizDescription,
		channelId = quizChannelId,
		authorId = quizAuthorId,
		createdAt = quizCreatedAt,     // nullable already matches DTO
		publishedAt = quizPublishedAt,
		version = quizVersion
	)

// From embedded QuizView (inside itemQuizzes JSON) to QuizDto
fun QuizView.toQuizDto(): QuizDto =
	QuizDto(
		id = quizId,
		title = quizTitle,
		description = quizDescription,
		channelId = quizChannelId,
		authorId = quizAuthorId,
		createdAt = quizCreatedAt,
		publishedAt = quizPublishedAt,
		version = version
	)

fun StoryView.toDto(): StoryDto = StoryDto(
	parts = parts.map {
		StoryDto.StoryPartDto(
			imageId = it.imageId,
			imageCaption = it.imageCaption,
			brief = it.brief
		)
	}
)


// --- Item mappers from views ---

fun QuizItemView.toItemDto(): ItemDto =
	ItemDto(
		id = itemId,
		title = itemTitle,
		description = itemDescription,
		imageId = itemImageId,
		question = itemQuestion,
		options = itemOptions,
		key = itemKey,
		story = itemStory?.toDto(),
		createdAt = itemCreatedAt,
		version = itemVersion,
		quizItemRelationVersion = quizItemRelationVersion
	)

fun QuizItemTagsView.toItemDto(): ItemDto =
	ItemDto(
		id = itemId,
		title = itemTitle,
		description = itemDescription,
		imageId = itemImageId,
		question = itemQuestion,
		options = itemOptions,
		key = itemKey,
		story = itemStory?.toDto(),
		createdAt = itemCreatedAt,
		version = itemVersion,
		quizItemRelationVersion = quizItemRelationVersion
	)

fun QuizItemTagsQuizzesView.toItemDto(): ItemDto =
	ItemDto(
		id = itemId,
		title = itemTitle,
		description = itemDescription,
		imageId = itemImageId,
		question = itemQuestion,
		options = itemOptions,
		key = itemKey,
		story = itemStory?.toDto(),
		createdAt = itemCreatedAt,
		version = itemVersion ?: error("itemVersion cannot be null"),
		quizItemRelationVersion = quizItemRelationVersion
	)

// Summary item
fun QuizItemSummaryView.toItemSummaryDto(): ItemSummaryDto =
	ItemSummaryDto(
		id = itemId,
		title = itemTitle,
		question = itemQuestion,
		imageId = itemImageId,
		version = itemVersion
	)

// Item + tags
fun QuizItemTagsView.toItemTagsDto(): ItemTagsDto =
	ItemTagsDto(
		item = this.toItemDto(),
		tags = itemTags
	)

// Item + tags + quizzes
fun QuizItemTagsQuizzesView.toItemTagsAndQuizzesDto(): ItemTagsQuizzesDto =
	ItemTagsQuizzesDto(
		item = this.toItemDto(),
		tags = itemTags,
		quizzes = itemQuizzes.map { it.toQuizDto() }
	)


// Helper to build a single QuizItemDto from rows of a single quiz
private fun List<QuizItemView>.toSingleQuizItemDto(): QuizItemDto {
	require(isNotEmpty()) { "Cannot map empty list to QuizItemDto" }
	val quiz = first().toQuizDto()
	val items = this
		.toMutableList().apply { sortBy { it.itemPos } }
		.map { it.toItemDto() }

	return QuizItemDto(
		quiz = quiz,
		items = items
	)
}

fun List<QuizItemView>.toQuizItemDtos(): List<QuizItemDto> =
	this
		.groupBy { it.quizId }
		.values
		.map { it.toSingleQuizItemDto() }

// --- With tags ---

fun List<QuizItemTagsView>.toSingleQuizItemTagsDto(): QuizItemTagsDto? {
	if (isEmpty()) return null
	val quiz = first().toQuizDto()
	val items = this
		.toMutableList().apply { sortBy { it.itemPos } }
		.map { it.toItemTagsDto() }

	return QuizItemTagsDto(
		quiz = quiz,
		items = items
	)
}

fun List<QuizItemTagsView>.toQuizItemTagsDtos(): List<QuizItemTagsDto> =
	this
		.groupBy { it.quizId }
		.values
		.mapNotNull { it.toSingleQuizItemTagsDto() }

// --- Summary view ---

private fun List<QuizItemSummaryView>.toSingleQuizItemSummaryDto(): QuizItemSummaryDto {
	require(isNotEmpty())
	val quiz = first().toQuizDto()
	val items = this
		.toMutableList().apply { sortBy { it.itemPos } }
		.map { it.toItemSummaryDto() }

	return QuizItemSummaryDto(
		quiz = quiz,
		items = items
	)
}

fun List<QuizItemSummaryView>.toQuizItemSummaryDtos(): List<QuizItemSummaryDto> =
	this
		.groupBy { it.quizId }
		.values
		.map { it.toSingleQuizItemSummaryDto() }

// --- With tags + quizzes ---

private fun List<QuizItemTagsQuizzesView>.toSingleQuizItemTagsQuizzesDto(): QuizItemTagsQuizzesDto {
	require(isNotEmpty())
	val quiz = first().toQuizDto()
	val items = this
		.toMutableList().apply { sortBy { it.itemPos } }
		.map { it.toItemTagsAndQuizzesDto() }

	return QuizItemTagsQuizzesDto(
		quiz = quiz,
		items = items
	)
}

fun List<QuizItemTagsQuizzesView>.toQuizItemTagsQuizzesDtos(): List<QuizItemTagsQuizzesDto> =
	this
		.groupBy { it.quizId }
		.values
		.map { it.toSingleQuizItemTagsQuizzesDto() }
