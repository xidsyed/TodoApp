package com.example.todoapp.common.config

import com.example.todoapp.app.auth.roles.data.converter.RoleEntityToStringConverter
import com.example.todoapp.app.auth.roles.data.converter.StringToRoleEntityConverter
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver

@Configuration
class R2dbcConfig {
	@Bean
	fun r2dbcCustomConversions(connectionFactory: ConnectionFactory): R2dbcCustomConversions {
		val dialect = DialectResolver.getDialect(connectionFactory)
		val converters = listOf(
			StringToRoleEntityConverter(),
			RoleEntityToStringConverter()
		)
		return R2dbcCustomConversions.of(dialect, converters)
	}
}
