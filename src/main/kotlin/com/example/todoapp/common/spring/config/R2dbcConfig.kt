package com.example.todoapp.common.spring.config

import com.example.todoapp.app.auth.roles.data.converter.*
import com.example.todoapp.common.converter.*
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.*
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver

@Configuration
class R2dbcConfig {
	@Bean
	fun r2dbcCustomConversions(connectionFactory: ConnectionFactory): R2dbcCustomConversions {
		val dialect = DialectResolver.getDialect(connectionFactory)
		val converters = listOf(
			StringToRoleEntityConverter(),
			RoleEntityToStringConverter(),
			InstantToOffsetDateTimeConverter(),
			OffsetDateTimeToInstantConverter()
		)
		return R2dbcCustomConversions.of(dialect, converters)
	}
}
