package com.example.todoapp.common.serialize

interface Serializer<T> {
	fun serialize(obj: T): String
	fun deserialize(s: String): T
}
