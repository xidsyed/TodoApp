package com.example.todoapp.app.quiz.domain.service

import com.example.todoapp.app.quiz.repository.ChannelRepository
import org.springframework.stereotype.Service

@Service
class ChannelService(
	private val channelRepo: ChannelRepository
) {

	suspend fun createChannel() {

	}

	suspend fun deleteChannel() {
		// deletes are restricted till published quizzes are unassigned
	}

}