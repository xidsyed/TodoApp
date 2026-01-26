package com.example.todoapp.common.spring.config

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.*
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.*
import org.springframework.web.method.HandlerMethod
import java.util.stream.Collectors

@Configuration
class SpringDocConfig {

	@Bean
	fun customOpenAPI(): OpenAPI =
		OpenAPI().info(
			Info()
				.title("Newzer API")
				.description("Resource server API for Newzer")
				.version("v1")
		)


	@Bean
	fun api(): GroupedOpenApi? {
		return GroupedOpenApi.builder()
			.group("default")
			.pathsToMatch("/**")
			.addOperationCustomizer { operation: Operation?, _: HandlerMethod? ->
				val newTags = operation!!.tags.stream()
					.map { t: String? -> t!!.replace("-controller$".toRegex(), "").replace('-', ' ') }
					.map { obj: String? -> obj!!.trim { it <= ' ' } }
					.collect(Collectors.toList())
				operation.tags = newTags
				operation
			}
			.build()
	}


	/**
	 * ModelResolver that uses the Jackson 2 ObjectMapper above.
	 * springdoc / swagger-core will pick this up for schema generation.
	 */
	@Bean
	fun modelResolver(): ModelResolver {
		val kotlinModule = KotlinModule.Builder()
			.configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
			.build()

		val swaggerObjectMapper = JsonMapper.builder()
			.findAndAddModules()
			.addModule(kotlinModule)
			.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build()

		return ModelResolver(swaggerObjectMapper)
	}


}

