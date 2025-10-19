package com.example.todoapp.core.util

import tools.jackson.module.kotlin.jsonMapper

fun interface PrettyPrintJson {
	operator fun invoke(body : Any) : String
}
