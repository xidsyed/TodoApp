package com.example.todoapp.core.serializer

interface Serializer<T> {
	fun serialize(obj: T): String
	fun deserialize(s: String): T
}


