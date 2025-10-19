package com.example.todoapp.common.config

import org.springframework.context.annotation.*
import tools.jackson.databind.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.*

@Configuration
class JacksonConfig {
	@Bean
	@Primary
	fun jsonMapper(): JsonMapper =
		JsonMapper.builder()
			.addModule(
				KotlinModule.Builder()
					.configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
					.build()
			)
			.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build()

}