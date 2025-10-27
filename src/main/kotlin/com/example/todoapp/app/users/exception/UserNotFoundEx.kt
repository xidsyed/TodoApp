package com.example.todoapp.app.users.exception

import com.example.todoapp.common.exception.NotFoundEx
import java.util.*

class UserNotFoundEx(id: UUID) : NotFoundEx("User", id.toString())