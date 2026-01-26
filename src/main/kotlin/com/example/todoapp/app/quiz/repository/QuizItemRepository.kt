package com.example.todoapp.app.quiz.repository

import com.example.todoapp.app.quiz.model.entity.QuizItemEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface QuizItemRepository : CoroutineCrudRepository<QuizItemEntity, UUID> {
	@Query("SELECT * FROM public.quiz_items WHERE deleted_at IS NULL ORDER BY created_at DESC")
	fun findAllActive(): Flow<QuizItemEntity>
}

