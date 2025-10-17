package com.example.todoapp.common.exception.handler

import com.example.todoapp.core.webhook.exception.DuplicateWebhookException
import com.example.todoapp.core.webhook.exception.WebhookSigningException
import com.example.todoapp.core.webhook.exception.WebhookSourceNotFoundException
import com.example.todoapp.core.webhook.exception.WebhookVerificationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WebhookControllerAdvice {

    private val logger = LoggerFactory.getLogger(WebhookControllerAdvice::class.java)

    @ExceptionHandler(DuplicateWebhookException::class)
    fun handleDuplicateWebhookException(ex: DuplicateWebhookException): ResponseEntity<Any> {
        logger.warn("Duplicate webhook received: ${ex.message}")
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ex.payload)
    }

    @ExceptionHandler(WebhookSigningException::class)
    fun handleWebhookSigningException(ex: WebhookSigningException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.TEXT_PLAIN)
            .body(ex.message ?: "Webhook signing error")
    }

    @ExceptionHandler(WebhookSourceNotFoundException::class)
    fun handleWebhookSourceNotFoundException(ex: WebhookSourceNotFoundException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.TEXT_PLAIN)
            .body(ex.message ?: "Webhook source not found")
    }

    @ExceptionHandler(WebhookVerificationException::class)
    fun handleWebhookVerificationException(ex: WebhookVerificationException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.TEXT_PLAIN)
            .body(ex.message ?: "Webhook verification failed")
    }

}