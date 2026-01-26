package com.example.todoapp.app.quiz.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.util.*

@Table("channels")
data class ChannelEntity(
	@Id
	@Column("id")
	val id: UUID? = null,

	@Column("name")
	val name: String,

	@Column("description")
	val description: String? = null,

	@Column("created_at")
	val createdAt: Instant? = null,

	@Column("updated_at")
	val updatedAt: Instant? = null
)