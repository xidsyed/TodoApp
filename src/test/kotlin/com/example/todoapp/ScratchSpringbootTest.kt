package com.example.todoapp

import com.example.todoapp.app.invitation.*
import com.example.todoapp.app.users.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
class ScratchSpringbootTest @Autowired constructor(
	private val userRepository: UserRepository,
	private val invitationRepository: InvitationRepository,
	private val invitationViewRepository: InvitationViewRepository,
	private val databaseClient: DatabaseClient,
	private val client: WebTestClient
) {


}