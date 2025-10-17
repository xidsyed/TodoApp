package com.example.todoapp.common.model

import com.fasterxml.jackson.annotation.JsonValue

enum class NewzroomRoleDto(@JsonValue val value: String) {
    WRITER("writer"),
    ADMIN("admin")
}
