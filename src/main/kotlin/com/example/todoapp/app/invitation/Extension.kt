package com.example.todoapp.app.invitation

import com.example.todoapp.common.exception.CommonNotFoundException

fun invitationNotFound(id: String) = CommonNotFoundException("Invitation", id)
