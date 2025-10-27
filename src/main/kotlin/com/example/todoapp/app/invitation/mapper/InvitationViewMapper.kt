package com.example.todoapp.app.invitation.mapper

import com.example.todoapp.app.auth.roles.data.mapper.dto
import com.example.todoapp.app.invitation.model.*
import com.example.todoapp.app.users.model.UserDto

fun InvitationView.dto() = InvitationDto(
    id = id,
    email = email,
    assignor = UserDto(
        id = assignorId,
        name = assignorName,
        picture = assignorPicture,
        role = assignorRole.dto()
    ),
    role = role.dto(),
    eat = eat,
    assignee = if (assigneeId != null && assigneeName != null && assigneeRole != null) {
        UserDto(
            id = assigneeId,
            name = assigneeName,
            picture = assigneePicture,
            role = assigneeRole.dto()
        )
    } else {
        null
    },
    createdAt = createdAt,
)
