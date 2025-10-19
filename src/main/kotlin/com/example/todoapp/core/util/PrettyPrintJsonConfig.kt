package com.example.todoapp.core.util

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class PrettyPrintJsonConfig {
	@Bean
	fun prettyPrintJson(jsonMapper: JsonMapper): PrettyPrintJson {
		return PrettyPrintJson {
			val body = it.toString()
			if (body.isBlank()) return@PrettyPrintJson body
			return@PrettyPrintJson try {
				jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readTree(body))
			} catch (e: Exception) {
				body
			}
		}
	}
}