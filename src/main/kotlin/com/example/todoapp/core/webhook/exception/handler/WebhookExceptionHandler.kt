package com.example.todoapp.core.webhook.exception.handler

import com.example.todoapp.core.webhook.exception.*
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.*

@RestControllerAdvice
class WebhookExceptionHandler {

    private val logger = LoggerFactory.getLogger(WebhookExceptionHandler::class.java)

    @ExceptionHandler(WebhookException::class)
    fun handleWebhookException(ex: WebhookException): ResponseEntity<Any> {
        return when (ex) {
            is DuplicateWebhookException -> {
                logger.warn("Duplicate webhook received: ${ex.message}")
                ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.payload)
            }
            is WebhookSigningException -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.message ?: "Webhook signing error")
            }
            is WebhookSourceNotFoundException -> {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.message ?: "Webhook source not found")
            }
            is WebhookVerificationException -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.message ?: "Webhook verification failed")
            }
        }
    }
}