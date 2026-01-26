package com.example.todoapp.common.spring.config

import com.example.todoapp.app.auth.roles.data.converter.*
import com.example.todoapp.app.quiz.converter.*
import com.example.todoapp.common.converter.*
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.*
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import tools.jackson.databind.json.JsonMapper

@Configuration
class R2dbcConfig {
	@Bean
	fun r2dbcCustomConversions(connectionFactory: ConnectionFactory, jsonMapper: JsonMapper): R2dbcCustomConversions {
		val dialect = DialectResolver.getDialect(connectionFactory)
		val converters = listOf(
			// RoleEntity <-> String
			StringToRoleEntityConverter(),
			RoleEntityToStringConverter(),
			// Instant <-> OffsetDateTime
			InstantToOffsetDateTimeConverter(),
			OffsetDateTimeToInstantConverter(),

			// Story <-> String
			StoryToJsonConverter(jsonMapper),
			JsonToStoryConverter(jsonMapper),

			// QuizViewList <-> String
			QuizViewListToJsonConverter(jsonMapper),
			JsonToQuizViewJsonConverter(jsonMapper)
		)
		return R2dbcCustomConversions.of(dialect, converters)
	}
}
