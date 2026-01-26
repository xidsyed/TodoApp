package com.example.todoapp.app.quiz.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.*
import java.time.Instant

@Table("tags")
data class TagEntity(
	@Id
	@Column("name")
	val name: String,
	@Column("created_at")
	val createdAt: Instant? = null
) : Persistable<String> {
	override fun getId() = name

	override fun isNew() = createdAt == null

}
