package com.example.todoapp.app.invitation

import com.example.todoapp.common.exception.NotFoundEx

fun invitationNotFound(id: String) = NotFoundEx("Invitation", id)
