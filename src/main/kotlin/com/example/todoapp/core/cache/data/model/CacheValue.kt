package com.example.todoapp.core.cache.data.model

import kotlin.time.Duration

class CacheValue<V : Any> (
	val value : V,
	val duration : Duration? = null
)