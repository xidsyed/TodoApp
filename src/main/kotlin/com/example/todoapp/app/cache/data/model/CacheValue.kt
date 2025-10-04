package com.example.todoapp.app.cache.data.model

import kotlin.time.Duration

class CacheValue<V : Any> (
	val value : V,
	val duration : Duration? = null
)