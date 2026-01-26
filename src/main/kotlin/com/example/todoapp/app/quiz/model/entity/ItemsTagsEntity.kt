package com.example.todoapp.app.quiz.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.*
import java.util.*

/**
 * Mapping table items_tags (item_id, tag_name) primary key composite.
 */
@Table("items_tags")
data class ItemsTagsEntity(
	@Id
	val itemTagId: ItemTagId,
) : Persistable<ItemTagId> {
	override fun getId(): ItemTagId? = itemTagId

	override fun isNew(): Boolean = true
}

data class ItemTagId(
	@Column("item_id")
	val item_id: UUID,

	@Column("tag_name")
	val tag_name: String,
)