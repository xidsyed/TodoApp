package com.example.todoapp.core.logging

import com.example.todoapp.core.logging.properties.LoggingWebFilterProperties
import com.example.todoapp.core.util.PrettyPrintJson
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import java.nio.charset.StandardCharsets

class ServerHttpLogger(
	private val properties: LoggingWebFilterProperties,
	private val prettyPrintJson: PrettyPrintJson
) {
	private val log = LoggerFactory.getLogger("RequestLogger")

	fun logRequest(request: ServerHttpRequest, bodyBytes: ByteArray) {
		val (bodyPreview, truncatedFlag) = formatBody(bodyBytes, request.headers)
		log.info(
			"\n<---- \nMETHOD: {}\nURL: {}\nHEADERS: {}\nBODY{}:\n{}\n",
			request.method, request.uri, request.headers, truncatedFlag, bodyPreview
		)
	}

	fun logResponse(response: ServerHttpResponse, bodyBytes: ByteArray?, startTime: Long) {
		val durationMs = (System.nanoTime() - startTime) / 1_000_000
		val (bodyPreview, truncatedFlag) = bodyBytes?.let {
			formatBody(it, response.headers)
		} ?: ("" to " (body not captured)")

		log.info(
			"\n---->\nSTATUS: {}\nDURATION: {} ms\nHEADERS: {}\nBODY{}:\n{}\n",
			response.statusCode, durationMs, response.headers, truncatedFlag, bodyPreview
		)
	}

	private fun formatBody(bodyBytes: ByteArray, headers: HttpHeaders): Pair<String, String> {
		if (bodyBytes.isEmpty()) return "" to ""

		val truncatedBody = if (bodyBytes.size > properties.maxBodyBytes) {
			bodyBytes.copyOfRange(0, properties.maxBodyBytes)
		} else {
			bodyBytes
		}

		val isJson = headers.contentType?.toString()?.contains("json", true) ?: false
		val body = String(truncatedBody, StandardCharsets.UTF_8).let { str ->
			if (isJson) prettyPrintJson(str) else str
		}
		val truncatedFlag = if (bodyBytes.size > properties.maxBodyBytes) " (truncated)" else ""

		return body to truncatedFlag
	}

}
