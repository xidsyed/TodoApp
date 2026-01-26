package com.example.todoapp.app.quiz.repository


import com.example.todoapp.app.quiz.model.entity.ChannelEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChannelRepository : CoroutineCrudRepository<ChannelEntity, UUID> { }

