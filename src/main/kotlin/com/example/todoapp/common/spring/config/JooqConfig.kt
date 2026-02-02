package com.example.todoapp.common.spring.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.*
import org.jooq.impl.*
import org.springframework.context.annotation.*
import org.springframework.context.annotation.Configuration

@Configuration
class JooqConfig(
	private val connectionFactory: ConnectionFactory
) {
	@Bean
	fun dslContext(): DSLContext =
		DSL.using(
			DefaultConfiguration()
				.set(SQLDialect.POSTGRES)
				.set(connectionFactory)
		)
}
