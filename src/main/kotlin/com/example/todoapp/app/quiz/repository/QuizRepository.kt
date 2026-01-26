package com.example.todoapp.app.quiz.repository

import com.example.todoapp.app.quiz.model.entity.QuizEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface QuizRepository : CoroutineCrudRepository<QuizEntity, UUID> {
	// Simple convenience query
	@Query("SELECT * FROM public.quizzes WHERE deleted_at IS NULL ORDER BY created_at DESC")
	fun findAllActive(): Flow<QuizEntity>
}

