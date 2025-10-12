package com.example.todoapp.common.model

import com.fasterxml.jackson.annotation.JsonValue

enum class NewzroomRole(@JsonValue val value: String) {
    WRITER("writer"),
    ADMIN("admin")
}
