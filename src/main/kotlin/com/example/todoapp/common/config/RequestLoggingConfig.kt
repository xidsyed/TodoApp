package com.example.todoapp.common.config

import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.*
import org.springframework.context.annotation.*
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.*
import org.springframework.http.server.reactive.*
import org.springframework.web.server.WebFilter
import reactor.core.publisher.*
import tools.jackson.databind.json.JsonMapper
import java.nio.charset.StandardCharsets

@ConfigurationProperties(prefix = "app.logging.requests")
data class RequestLoggingProperties(
	var enabled: Boolean = false,
	var maxBodyBytes: Int = 8192
)

@Configuration
@EnableConfigurationProperties(RequestLoggingProperties::class)
class RequestResponseLoggingConfig(private val props: RequestLoggingProperties) {
	private val log = LoggerFactory.getLogger("RequestLogger")

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	fun requestResponseLoggingFilter(jsonMapper: JsonMapper): WebFilter = WebFilter { exchange, chain ->
		if (!props.enabled) return@WebFilter chain.filter(exchange)

		val request = exchange.request
		val startTime = System.nanoTime()

		val contentType = request.headers.contentType?.toString() ?: ""
		val isTextual = contentType.startsWith("text") ||
				contentType.contains("json", true) ||
				contentType.contains("xml", true) ||
				contentType.contains("form", true)

		val isJson = contentType.contains("json", true)

		val requestBodyMono = if (isTextual) {
			DataBufferUtils.join(request.body)
				.defaultIfEmpty(DefaultDataBufferFactory().wrap(ByteArray(0)))
				.map { buffer ->
					val bytes = ByteArray(buffer.readableByteCount())
					buffer.read(bytes)
					DataBufferUtils.release(buffer)
					bytes
				}
		} else {
			Mono.just(ByteArray(0))
		}

		requestBodyMono.flatMap { requestBytes ->
			val truncated = if (requestBytes.size > props.maxBodyBytes)
				requestBytes.copyOfRange(0, props.maxBodyBytes)
			else requestBytes

			val bodyPreview = String(truncated, StandardCharsets.UTF_8).let {
				if (isJson) {
					prettyPrintJson(jsonMapper, it)
				} else it
			}

			val truncatedFlag = if (requestBytes.size > props.maxBodyBytes) " (truncated)" else ""

			log.info(
				"\n<---- \nMETHOD: {}\nURL: {}\nHEADERS: {}\nBODY{}:\n{}\n",
				request.method, request.uri, request.headers, truncatedFlag, bodyPreview
			)

			// Rebuild request body for downstream
			val requestDecorator = object : ServerHttpRequestDecorator(request) {
				override fun getBody(): Flux<DataBuffer> {
					val buffer = DefaultDataBufferFactory().wrap(requestBytes)
					return Flux.just(buffer)
				}
			}

			// Decorate response to log after downstream writes
			val responseDecorator = object : ServerHttpResponseDecorator(exchange.response) {
				override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
					val bodyFlux = Flux.from(body)
					return DataBufferUtils.join(bodyFlux)
						.flatMap { buffer ->
							val bytes = ByteArray(buffer.readableByteCount())
							buffer.read(bytes)
							DataBufferUtils.release(buffer)

							val truncatedResp = if (bytes.size > props.maxBodyBytes)
								bytes.copyOfRange(0, props.maxBodyBytes)
							else bytes

							val responseIsJson = exchange.response.headers.contentType?.toString()
								?.contains("json", true) ?: false

							val bodyString = String(truncatedResp, StandardCharsets.UTF_8).let {
								if (responseIsJson) prettyPrintJson(jsonMapper, it)
								else it
							}
							val respTruncatedFlag = if (bytes.size > props.maxBodyBytes) " (truncated)" else ""
							val durationMs = (System.nanoTime() - startTime) / 1_000_000

							log.info(
								"\n---->\nSTATUS: {}\nDURATION: {} ms\nHEADERS: {}\nBODY{}:\n{}\n",
								statusCode, durationMs, headers, respTruncatedFlag, bodyString
							)

							// Use the response's buffer factory (preferred)
							val newBuffer = exchange.response.bufferFactory().wrap(bytes)
							super.writeWith(Mono.just(newBuffer))
						}
						.switchIfEmpty(super.writeWith(Mono.empty()))
					// If join yields empty (no body), just delegate to super
				}

				// handle writeAndFlushWith too
				override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
					val concatenated = Flux.from(body).flatMap { Flux.from(it) }
					return writeWith(concatenated)
				}

			}

			val decoratedExchange = exchange.mutate()
				.request(requestDecorator)
				.response(responseDecorator)
				.build()

			chain.filter(decoratedExchange)
		}
	}

	private fun prettyPrintJson(jsonMapper: JsonMapper, body: String): String {
		if (body.isBlank()) return body
		return try {
			jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readTree(body))
		} catch (e: Exception) {
			body // Not a valid JSON or some other error, return as is
		}
	}
}
