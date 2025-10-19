package com.example.todoapp.app.auth.roles.data.model

import com.fasterxml.jackson.annotation.JsonValue

enum class NewzroomRole(@JsonValue val value: String) {
    WRITER("writer"),
    ADMIN("admin"),
	NONE("none")
}