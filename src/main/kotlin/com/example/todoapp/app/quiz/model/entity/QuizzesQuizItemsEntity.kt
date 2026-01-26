package com.example.todoapp.app.quiz.model.entity

import org.springframework.data.annotation.*
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.*
import java.time.Instant
import java.util.*

/**
 * Join table: primary key (quiz_id, item_id)
 */
@ConsistentCopyVisibility
@Table("quizzes_quiz_items")
data class QuizzesQuizItemsEntity private constructor(
	@Id
	val ids: QuizzesQuizItemsId,

	@Column("pos")
	val pos: Short,

	@Column("created_at")
	@CreatedDate
	val createdAt: Instant? = null,

	@Version
	@Column("version")
	val version: Int? = null

) : Persistable<QuizzesQuizItemsId> {

	companion object {
		fun new(quizId: UUID, itemId: UUID, pos: Short): QuizzesQuizItemsEntity {
			return QuizzesQuizItemsEntity(QuizzesQuizItemsId(quizId, itemId), pos).apply { isNew = true }
		}

		fun old(quizId: UUID, itemId: UUID, pos: Short, createdAt: Instant? = null, version: Int): QuizzesQuizItemsEntity {
			return QuizzesQuizItemsEntity(QuizzesQuizItemsId(quizId, itemId), pos, createdAt, version).apply {
				isNew = false
			}
		}
	}

	@Transient
	private var isNew: Boolean = true

	override fun getId(): QuizzesQuizItemsId = ids

	override fun isNew(): Boolean = isNew

}

data class QuizzesQuizItemsId(
	@Column("quiz_id")
	val quiz_id: UUID,

	@Column("item_id")
	val item_id: UUID,
)
