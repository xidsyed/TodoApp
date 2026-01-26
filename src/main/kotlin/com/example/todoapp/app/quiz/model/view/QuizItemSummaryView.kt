package com.example.todoapp.app.quiz.model.view

import org.springframework.data.relational.core.mapping.Column
import java.time.Instant
import java.util.*

data class QuizItemSummaryView(
	@Column("quiz_id") val quizId: UUID,
	@Column("quiz_title") val quizTitle: String,
	@Column("quiz_description") val quizDescription: String?,
	@Column("quiz_channel_id") val quizChannelId: UUID,
	@Column("quiz_author_id") val quizAuthorId: UUID?,
	@Column("quiz_created_at") val quizCreatedAt: Instant,
	@Column("quiz_published_at") val quizPublishedAt: Instant,
	@Column("quiz_version") val quizVersion: Int,

	@Column("item_pos") val itemPos: Short,
	@Column("quiz_item_relation_version") val quizItemRelationVersion: Int,

	@Column("item_id") val itemId: UUID,
	@Column("item_title") val itemTitle: String?,
	@Column("item_question") val itemQuestion: String,
	@Column("item_image_id") val itemImageId: String?,
	@Column("item_created_at") val itemCreatedAt: Instant,
	@Column("item_version") val itemVersion: Int
)