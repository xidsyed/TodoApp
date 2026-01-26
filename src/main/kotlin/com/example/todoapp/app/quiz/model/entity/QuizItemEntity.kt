package com.example.todoapp.app.quiz.model.entity

import com.example.todoapp.app.quiz.model.view.StoryView
import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.util.*

/**
 * notes:
 * - `options` maps text[] -> List<String>
 * - `story` (jsonb) mapped to String (raw JSON). If you prefer a typed model, replace with a data class and configure Jackson mapping.
 */
@Table("quiz_items")
data class QuizItemEntity(
	@Id
	@Column("id")
	val id: UUID? = null,

	@Column("title")
	val title: String? = null,

	@Column("description")
	val description: String? = null,

	@Column("image_id")
	val imageId: String? = null,

	@Column("question")
	val question: String,

	@Column("options")
	val options: Array<String>,

	@Column("key")
	val key: Short,

	// store raw json - keeps things simple with R2DBC
	@Column("story")
	val story: StoryView? = null,

	@Column("created_at")
	val createdAt: Instant? = null,

	@Column("updated_at")
	val updatedAt: Instant? = null,

	@Column("deleted_at")
	val deletedAt: Instant? = null,

	@Version
	@Column("version")
	val version: Int? = null
)

