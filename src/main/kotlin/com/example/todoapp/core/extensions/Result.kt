package com.example.todoapp.core.extensions

import com.github.michaelbull.result.*
import kotlinx.coroutines.coroutineScope
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.reactive.*


suspend fun <V, E> TransactionalOperator.executeAndAwaitResult(
	rollbackOnError: suspend (E) -> Boolean = { true },
	action: suspend (ReactiveTransaction) -> Result<V, E>
): Result<V, E> = executeAndAwait { tx ->
	coroutineScope {
		action(tx)
	}.onFailure { err ->
		if (rollbackOnError(err)) {
			tx.setRollbackOnly()
		}
	}
}