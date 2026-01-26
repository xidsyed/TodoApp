package com.example.todoapp.app.quiz.model.entity

import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.util.*

@Table("quizzes")
data class QuizEntity(
	@Id
	@Column("id")
	val id: UUID? = null,

	@Column("title")
	val title: String,

	@Column("description")
	val description: String? = null,

	@Column("channel_id")
	val channelId: UUID,

	@Column("author_id")
	val authorId: UUID? = null,

	@Column("created_at")
	val createdAt: Instant? = null,

	@Column("updated_at")
	val updatedAt: Instant? = null,

	@Column("published_at")
	val publishedAt: Instant,

	@Column("deleted_at")
	val deletedAt: Instant? = null,

	@Version
	@Column("version")
	val version: Int? = null
)
