package com.example.todoapp.core.logging.config

import com.example.todoapp.core.logging.ServerHttpLogger
import com.example.todoapp.core.logging.filter.LoggingWebFilter
import com.example.todoapp.core.logging.properties.LoggingWebFilterProperties
import com.example.todoapp.core.util.PrettyPrintJson
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.server.WebFilter

@Configuration
@EnableConfigurationProperties(LoggingWebFilterProperties::class)
class LoggingWebFilterConfig {

	@Bean
	fun httpConsoleLogger(
		props: LoggingWebFilterProperties,
		prettyPrintJson: PrettyPrintJson
	): ServerHttpLogger = ServerHttpLogger(properties = props, prettyPrintJson = prettyPrintJson)

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	fun requestLoggingFilter(
		props: LoggingWebFilterProperties,
		logger: ServerHttpLogger
	): WebFilter = LoggingWebFilter(props, logger)
}