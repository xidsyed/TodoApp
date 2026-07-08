package com.example.todoapp.common.util

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.jooq.DSLContext
import org.jooq.kotlin.coroutines.transactionCoroutine


suspend inline fun <T> DSLContext.tx(crossinline block: suspend DSLContext.() -> T): T {
	return transactionCoroutine {
		it.dsl().block()
	}
}

suspend fun <T> DSLContext.ex(block: suspend DSLContext.() -> T): T {
	return this.block()
}

suspend inline fun <reified R : Any> org.reactivestreams.Publisher<out org.jooq.Record>.awaitAllAs(): List<R> {
	return asFlow()
		.map { record -> record.into(R::class.java) }
		.toList()
}

suspend fun <T : Record, R : Any> org.reactivestreams.Publisher<T>.awaitAll(): List<T> {
	return this.asFlow().toList()
}

