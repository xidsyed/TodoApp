package com.example.todoapp.app.quiz.repository


import com.example.todoapp.app.quiz.model.entity.TagEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : CoroutineCrudRepository<TagEntity, String> {}

