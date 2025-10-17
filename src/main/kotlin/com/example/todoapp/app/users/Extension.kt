package com.example.todoapp.app.users

import com.example.todoapp.common.exception.CommonNotFoundException

fun userNotFound(id: String) = CommonNotFoundException("User Profile" , id)