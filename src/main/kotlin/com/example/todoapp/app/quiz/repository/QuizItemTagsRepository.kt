package com.example.todoapp.app.quiz.repository

import com.example.todoapp.app.quiz.model.entity.*
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ItemsTagsRepository : CoroutineCrudRepository<ItemsTagsEntity, ItemTagId> {

	@Query("SELECT * FROM items_tags WHERE item_id = :itemId")
	fun getByItemId(itemId: UUID): Flow<ItemsTagsEntity>

	@Query("SELECT * FROM items_tags WHERE item_id IN (:itemIds)")
	fun getByItemIds(itemIds: Iterable<UUID>): Flow<ItemsTagsEntity>


}