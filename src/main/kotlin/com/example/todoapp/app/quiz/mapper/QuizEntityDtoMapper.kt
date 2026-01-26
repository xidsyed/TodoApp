package com.example.todoapp.app.quiz.mapper


import com.example.todoapp.app.quiz.model.dto.*
import com.example.todoapp.app.quiz.model.entity.*
import com.example.todoapp.app.quiz.model.view.StoryView
import java.time.Instant


fun QuizEntity.toDto(): QuizDto =
	QuizDto(
		id = requireNotNull(id) { "QuizEntity.id is null; cannot map to QuizDto" },
		title = title,
		description = description,
		channelId = channelId,
		authorId = authorId,
		createdAt = requireNotNull(createdAt) { "QuizEntity.createdAt is null; cannot map to QuizDto" },
		publishedAt = publishedAt,
		version = requireNotNull(version) { "QuizEntity.version is null; cannot map to QuizDto" }
	)

fun QuizDto.toEntity(createdAt: Instant?, deletedAt: Instant?): QuizEntity =
	QuizEntity(
		id = id,                    // assuming DTO is only used for persisted quizzes
		title = title,
		description = description,
		channelId = channelId,
		authorId = authorId,
		createdAt = createdAt,
		publishedAt = publishedAt,
		deletedAt = deletedAt,
		version = version
	)


fun StoryDto.toView(): StoryView = StoryView(
	parts = parts.map {
		StoryView.StoryPartView(
			imageId = it.imageId,
			imageCaption = it.imageCaption,
			brief = it.brief
		)
	}
)

fun QuizItemEntity.toDto(quizItemRelationVersion: Int): ItemDto =
	ItemDto(
		id = requireNotNull(id) { "QuizItemEntity.id is null; cannot map to ItemDto" },
		title = title,
		description = description,
		imageId = imageId,
		question = question,
		options = options.toList(),
		key = key,
		story = story?.toDto(),
		createdAt = requireNotNull(createdAt) { "QuizItemEntity.createdAt is null; cannot map to ItemDto" },
		version = requireNotNull(version) { "QuizItemEntity.version is null; cannot map to ItemDto" },
		quizItemRelationVersion = quizItemRelationVersion
	)

fun ItemDto.toEntity(createdAt: Instant?, deletedAt: Instant?): QuizItemEntity =
	QuizItemEntity(
		id = if (version == 0) null else id,
		title = title,
		description = description,
		imageId = imageId,
		question = question,
		options = options.toTypedArray(),
		key = key,
		story = story?.toView(),
		createdAt = createdAt,
		deletedAt = deletedAt,
		version = version

	)

