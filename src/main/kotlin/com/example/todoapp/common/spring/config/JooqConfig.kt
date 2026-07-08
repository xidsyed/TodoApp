package com.example.todoapp.common.spring.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.*
import org.jooq.impl.*
import org.springframework.context.annotation.*
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy

@Configuration
class JooqConfig(
	private val connectionFactory: ConnectionFactory
) {
	@Bean
	fun dslContext(): DSLContext {
		val proxy = TransactionAwareConnectionFactoryProxy(connectionFactory)

		// Create configuration with both the proxy AND the Reactor-aware subscriber provider
		val config = DefaultConfiguration().apply {
			setConnectionFactory(proxy)
			setSQLDialect(SQLDialect.POSTGRES) // or your dialect
		}

		return DSL.using(config)
	}
}
