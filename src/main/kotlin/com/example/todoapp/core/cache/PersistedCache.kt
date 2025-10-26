package com.example.todoapp.core.cache

import kotlinx.coroutines.flow.Flow
import java.time.Duration

interface PersistedCache<K, V> {
	suspend fun putIfAbsent(k :K, v: V, duration: Duration? = null)
	suspend fun put(k: K, v: V, duration: Duration? = null)
	suspend fun get(k: K): V?
	fun getAll() : Flow<Pair<K, V>>
	suspend fun clearCache() : Unit
	suspend fun remove(k: K)
}
