package com.example.todoapp.common.spring.config

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.*
import tools.jackson.databind.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.*

@Configuration
class JacksonConfig {

	@Bean
	@Primary
	fun jsonMapper(): JsonMapper {
		val kotlinModule = KotlinModule.Builder()
			.configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
			.build()

		val mapper = JsonMapper.builder()
			.findAndAddModules()
			.addModule(kotlinModule)
			.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build()

		return mapper
	}

	@Bean
	fun jacksonCustomizer() = JsonMapperBuilderCustomizer { builder ->
		builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
	}


}