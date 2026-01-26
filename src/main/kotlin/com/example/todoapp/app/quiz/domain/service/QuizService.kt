package com.example.todoapp.app.quiz.domain.service

import com.example.todoapp.app.quiz.mapper.*
import com.example.todoapp.app.quiz.model.dto.*
import com.example.todoapp.app.quiz.model.entity.*
import com.example.todoapp.app.quiz.repository.*
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.common.exception.DomainEx
import com.example.todoapp.core.extensions.executeAndAwaitResult
import com.github.michaelbull.result.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.*
import kotlin.random.Random


@Service
class QuizService(
	private val quizItemRepo: QuizItemRepository,
	private val quizRepo: QuizRepository,
	private val tagRepo: TagRepository,
	private val itemsTagsRepo: ItemsTagsRepository,
	private val quizzesQuizItemsRepo: QuizzesQuizItemsRepository,
	private val channelRepo: ChannelRepository,
	private val viewRepo: QuizViewsRepository,
	private val tx: TransactionalOperator,
	private val userRepo: UserRepository,
	private val channelValidationService: ChannelValidationService
) {

	suspend fun upsertQuiz(request: QuizItemTagsDto): Result<QuizItemTagsDto, DomainEx> = tx.executeAndAwaitResult {
		val current = fetchAsQuizItemTagsDto(request.quiz.id)

		if (current == request) {
			return@executeAndAwaitResult Ok(current)
		}

		channelValidationService.validateRequest(request).onFailure {
			return@executeAndAwaitResult Err(it)
		}

		// SYNC `quiz` TABLE
		syncQuizTable(request)

		// SYNC `quiz_items` AND `tags` TABLES
		syncItemsTableAndTagsTable(request.items)

		// SYNC `items_tags` TABLE
		syncItemTagsTable(request.items)

		// SYNC `quizzes_quiz_items` TABLE
		syncQuizzesAndQuizItemsTable(request, current!!)

		Ok(fetchAsQuizItemTagsDto(request.quiz.id)!!)
	}

	suspend fun deleteQuiz(quizId: UUID) {
		TODO("To be implemented")
	}

	suspend fun deleteItem(itemId: UUID) {
		// Deletes of qi restricted in qqi table
		TODO("To be implemented")
	}

	private suspend fun syncQuizTable(request : QuizItemTagsDto) {
		quizRepo.findById(request.quiz.id).let { q ->
			val reqQuizEntity = request.quiz.toEntity(q?.createdAt, q?.deletedAt)

			if (q != null && q == reqQuizEntity) return@let
			quizRepo.save(reqQuizEntity)
		}
	}

	private suspend fun syncItemsTableAndTagsTable(reqItems: List<ItemTagsDto>) {
		val reqItemsById = reqItems.associateBy { it.item.id }

		// SYNC `quiz_items` TABLE
		val existingItemEntitiesById =
			quizItemRepo.findAllById(reqItems.map { it.item.id }).toList().associateBy { it.id }

		val itemEntitiesToUpdate = existingItemEntitiesById.values.mapNotNull { existingEntity ->
			val reqDto = reqItemsById[existingEntity.id]!!.item
			val reqItemEntity = reqDto.toEntity(existingEntity.createdAt, existingEntity.deletedAt)
			if (reqItemEntity == existingEntity) null else reqItemEntity
		}

		val itemEntitiesToInsert = reqItems
			.filter { !existingItemEntitiesById.contains(it.item.id) }
			.map { it.item.toEntity(null, null) }

		// SYNC TAGS TABLE
		val reqTags = reqItems.flatMap { it.tags }.distinct()
		val existingTags = tagRepo.findAllById(reqTags).map { it.name }.toSet()
		val newTags = reqTags.filter { it !in existingTags }

		quizItemRepo.saveAll(itemEntitiesToUpdate).collect()
		quizItemRepo.saveAll(itemEntitiesToInsert).collect()
		tagRepo.saveAll(newTags.map { TagEntity(it) }).collect()
	}

	private suspend fun syncItemTagsTable(reqItems: List<ItemTagsDto>) {
		val reqItemsById = reqItems.associateBy { it.item.id }

		val currItemTagSet = itemsTagsRepo.getByItemIds(reqItemsById.keys).map {
			it.itemTagId.item_id to it.itemTagId.tag_name
		}.toSet()

		val reqItemTagSet = reqItems.flatMap { it.tags.map { tag -> it.item.id to tag } }.toSet()

		val itemTagsToRemove = currItemTagSet - reqItemTagSet
		val itemTagsToInsert = reqItemTagSet - currItemTagSet

		coroutineScope {
			// deleteAll and  saveAll functions don't work with composite keys currently in spring-data-r2dbc
			itemTagsToRemove.forEach {
				launch { itemsTagsRepo.deleteById(ItemTagId(it.first, it.second)) }
			}
			itemTagsToInsert.map { ItemsTagsEntity(ItemTagId(it.first, it.second)) }.forEach {
				launch { itemsTagsRepo.save(it) }
			}
		}
	}

	private suspend fun syncQuizzesAndQuizItemsTable(request: QuizItemTagsDto, current: QuizItemTagsDto?) {
		val currItemIds = current?.items?.map { it.item.id }?.toSet() ?: emptySet()
		val reqItemIds = request.items.map { it.item.id }.toSet()

		// 1. entities to remove
		val entitiesToRemove = (currItemIds - reqItemIds).map { itemId ->
			QuizzesQuizItemsId(request.quiz.id, itemId)
		}
		// 2. entities to add
		val entitiesToAdd = (reqItemIds - currItemIds).map { itemId ->
			QuizzesQuizItemsEntity.new(request.quiz.id, itemId, Random.nextInt().toShort())
		}

		coroutineScope {
			entitiesToRemove.forEach { launch { quizzesQuizItemsRepo.deleteById(it) } }
			entitiesToAdd.forEach { launch { quizzesQuizItemsRepo.save(it) } }
		}

		// 3. update all positions
		request.items.forEachIndexed { index, dto ->
			quizzesQuizItemsRepo.save(
				QuizzesQuizItemsEntity.old(request.quiz.id, dto.item.id, index.toShort(), createdAt = null, version= dto.item.quizItemRelationVersion)
			)
		}
	}

	private suspend fun fetchAsQuizItemTagsDto(quizId: UUID): QuizItemTagsDto? {
		return viewRepo.findQuizWithItemTagsById(quizId).toList().toSingleQuizItemTagsDto()
	}

}


