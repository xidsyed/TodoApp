package com.example.todoapp.common.data.entity

enum class NewzroomRoleEntity(val value: String) {
	WRITER("writer"),
	ADMIN("admin");

	companion object {
		fun from(value: String): NewzroomRoleEntity =
			entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
				?: throw IllegalArgumentException("Unknown role: $value")
	}

	override fun toString(): String = value

}