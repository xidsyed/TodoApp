package com.example.todoapp.app.invitation

import com.example.todoapp.app.invitation.entity.InvitationEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface InvitationRepository : CoroutineCrudRepository<InvitationEntity, UUID>