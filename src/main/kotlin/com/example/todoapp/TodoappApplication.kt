package com.example.todoapp

import org.slf4j.LoggerFactory
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean


@SpringBootApplication
@EnableConfigurationProperties
class TodoappApplication {

	private val logger = LoggerFactory.getLogger(TodoappApplication::class.java)
	@Bean
	fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
		return CommandLineRunner {}
	}
}

fun main(args: Array<String>) {
	runApplication<TodoappApplication>(*args)
}
