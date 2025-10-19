package com.example.todoapp.core.logging.filter

import com.example.todoapp.core.logging.ServerHttpLogger
import com.example.todoapp.core.logging.properties.LoggingWebFilterProperties
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream

class LoggingWebFilter(
	private val props: LoggingWebFilterProperties,
	private val logger: ServerHttpLogger
) : WebFilter {

	override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
		if (!props.enabled) {
			return chain.filter(exchange)
		}

		return mono {
			val startTime = System.nanoTime()

			val (request, requestBody) = if (isTextual(exchange.request)) {
				val body = readRequestBody(exchange.request)
				RequestDecorator(exchange.request, body) to body
			} else {
				exchange.request to ByteArray(0)
			}

			logger.logRequest(exchange.request, requestBody)

			val response = ResponseDecorator(exchange.response, logger, startTime)

			chain.filter(
				exchange.mutate()
					.request(request)
					.response(response)
					.build()
			).awaitSingleOrNull()
		}.then()
	}


	private fun isTextual(request: ServerHttpRequest): Boolean {
		val contentType = request.headers.contentType?.toString() ?: ""
		return contentType.startsWith("text") ||
				contentType.contains("json", true) ||
				contentType.contains("xml", true) ||
				contentType.contains("form", true)
	}

	private suspend fun readRequestBody(request: ServerHttpRequest): ByteArray {
		val output = ByteArrayOutputStream()
		request.body.asFlow().collect { buffer ->
			try {
				val bytes = ByteArray(buffer.readableByteCount())
				buffer.read(bytes)
				output.write(bytes)
			} finally {
				DataBufferUtils.release(buffer)
			}
		}
		return output.toByteArray()
	}
}