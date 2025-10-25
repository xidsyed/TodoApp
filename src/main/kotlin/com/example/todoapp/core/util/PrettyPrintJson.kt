package com.example.todoapp.core.util

fun interface PrettyPrintJson {
	operator fun invoke(body : Any) : String
}
