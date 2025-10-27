package com.example.todoapp.app.invitation

import com.example.todoapp.app.invitation.entity.InvitationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InvitationRepository : CoroutineCrudRepository<InvitationEntity, UUID> {
	@Query("SELECT * FROM public.invitations WHERE assignee = :uuid")
	suspend fun findByAssignee(uuid: UUID): InvitationEntity?

	@Query("SELECT * FROM public.invitations WHERE assignor = :uuid")
	suspend fun findByAssignor(uuid: UUID): InvitationEntity?

	@Query("SELECT * FROM public.invitations WHERE email = :email")
	suspend fun findByEmail(email: String): InvitationEntity?
}