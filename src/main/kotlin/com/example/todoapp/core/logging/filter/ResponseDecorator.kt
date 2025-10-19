package com.example.todoapp.core.logging.filter

import com.example.todoapp.core.logging.ServerHttpLogger
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicBoolean

class ResponseDecorator(
    private val response: ServerHttpResponse,
    private val logger: ServerHttpLogger,
    private val startTime: Long
) : ServerHttpResponseDecorator(response) {
    private val logged = AtomicBoolean(false)

    init {
        // beforeCommit fallback — will run only if writeWith didn't set `logged` earlier.
        beforeCommit {
            if (logged.compareAndSet(false, true)) {
                // body not captured path — body argument null means "no body captured"
                logger.logResponse(this@ResponseDecorator, null, startTime)
            }
            Mono.empty()
        }
    }

    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        // Important: set logged early to avoid beforeCommit racing and logging a default 200
        // when the real log will be done in writeWith.
        logged.set(true)

        return mono {
            val dataBuffer = DataBufferUtils.join(body).awaitSingleOrNull()
            val bytes = dataBuffer?.let {
                val b = ByteArray(it.readableByteCount())
                it.read(b)
                DataBufferUtils.release(it)
                b
            }

            logger.logResponse(response, bytes, startTime)

            val publisher = bytes?.let {
                Mono.just(response.bufferFactory().wrap(it))
            } ?: Mono.empty()
            super.writeWith(publisher).awaitSingleOrNull()
        }
    }

    override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
        // Flatten nested publishers and delegate to writeWith
        val concatenated = fluxFromPublisher(body).flatMap { fluxFromPublisher(it) }
        return writeWith(concatenated)
    }

    // Tiny helper to convert Publisher to Flux without importing Flux directly in signature
    // (use Reactor's Flux.from when available in your file)
    private fun <T> fluxFromPublisher(p: Publisher<T>) = Flux.from(p)
}