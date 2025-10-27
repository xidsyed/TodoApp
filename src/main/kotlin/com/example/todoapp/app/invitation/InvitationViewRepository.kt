package com.example.todoapp.app.invitation

import com.example.todoapp.app.invitation.model.InvitationView
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InvitationViewRepository : CoroutineCrudRepository<InvitationView, UUID> {

    @Query("""
        SELECT
            i.id,
            i.email,
            i.role,
            i.eat,
            i.created_at as "created_at",
            assignor.id as "assignor_id",
            assignor.display_name as "assignor_name",
            assignor.profile_pic as "assignor_picture",
            assignor.role as "assignor_role",
            assignee.id as "assignee_id",
            assignee.display_name as "assignee_name",
            assignee.profile_pic as "assignee_picture",
            assignee.role as "assignee_role"
        FROM
            invitations i
        JOIN
            user_profiles assignor ON i.assignor = assignor.id
        LEFT JOIN
            user_profiles assignee ON i.assignee = assignee.id
    """)
    fun findViewAll(): Flow<InvitationView>

	@Query("""
        SELECT
            i.id,
            i.email,
            i.role,
            i.eat,
            i.created_at as "created_at",
            assignor.id as "assignor_id",
            assignor.display_name as "assignor_name",
            assignor.profile_pic as "assignor_picture",
            assignor.role as "assignor_role",
            assignee.id as "assignee_id",
            assignee.display_name as "assignee_name",
            assignee.profile_pic as "assignee_picture",
            assignee.role as "assignee_role"
        FROM
            invitations i
        JOIN
            user_profiles assignor ON i.assignor = assignor.id
        LEFT JOIN
            user_profiles assignee ON i.assignee = assignee.id
		WHERE
			i.id = :id
    """)
	suspend fun findViewById(id: UUID) : InvitationView?

}