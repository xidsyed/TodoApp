package com.example.todoapp.core.logging.filter

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux

class RequestDecorator(
    delegate: ServerHttpRequest,
    private val body: ByteArray
) : ServerHttpRequestDecorator(delegate) {
    override fun getBody(): Flux<DataBuffer> {
        return Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(body))
    }
}
