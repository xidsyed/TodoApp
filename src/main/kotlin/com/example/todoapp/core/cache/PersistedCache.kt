package com.example.todoapp.core.cache

import kotlin.time.Duration

interface PersistedCache<K, V> {
	suspend fun putIfAbsent(k :K, v: V, duration: Duration? = null)
	suspend fun put(k: K, v: V, duration: Duration? = null)
	suspend fun get(k: K): V?
	suspend fun remove(k: K)
}
