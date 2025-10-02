package com.example.todoapp.app.cache.domain

import kotlin.time.Duration

interface PersistedCache<K, V> {
	suspend fun put(k: K, v: V, ttl: Duration? = null)
	suspend fun get(k: K): V?
	suspend fun remove(k: K)
}

