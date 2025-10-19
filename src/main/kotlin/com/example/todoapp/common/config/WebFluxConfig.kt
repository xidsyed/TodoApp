package com.example.todoapp.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.*
import org.springframework.web.reactive.config.WebFluxConfigurer
import tools.jackson.databind.json.JsonMapper


@Configuration
class WebFluxConfig ( private val jsonMapper: JsonMapper) : WebFluxConfigurer {
	override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
		configurer.defaultCodecs().apply {
			jacksonJsonEncoder(JacksonJsonEncoder(jsonMapper)) // replace encoder
			jacksonJsonDecoder(JacksonJsonDecoder(jsonMapper))
			maxInMemorySize(2 * 1024 * 1024) // 2MB buffer for JSON bodies
		}
	}
}
