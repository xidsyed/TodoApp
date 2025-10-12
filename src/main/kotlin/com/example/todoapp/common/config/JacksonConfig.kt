package com.example.todoapp.common.config

import org.springframework.context.annotation.*
import tools.jackson.databind.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.*

@Configuration
class JacksonConfig {
	/*
	@Bean
	@Primary
	fun defaultObjectMapper(): ObjectMapper = jacksonObjectMapper().registerKotlinModule {
		// https://github.com/FasterXML/jackson-module-kotlin/issues/630
		this.configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
	}.apply {
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
	}
	*/

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