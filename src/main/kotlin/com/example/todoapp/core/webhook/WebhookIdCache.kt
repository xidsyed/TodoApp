package com.example.todoapp.core.webhook

import com.example.todoapp.core.cache.*
import com.example.todoapp.core.cache.data.PersistedCacheImpl
import com.example.todoapp.core.serializer.jacksonSerializer
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import kotlin.time.Duration.Companion.seconds


@Component
class WebhookIdCache @Autowired constructor(
	cachePersistenceRepository: CachePersistenceRepository,
	jsonMapper: JsonMapper,
) : PersistedCache<String, Boolean> by PersistedCacheImpl(
	persistence = cachePersistenceRepository,
	cacheId = "webhook_cache",
	keySerializer = jacksonSerializer<String>(jsonMapper),
	valueSerializer = jacksonSerializer<Boolean>(jsonMapper),
	defaultDuration = Webhook.TOLERANCE_IN_SECONDS.seconds,
	cacheSize = 1_000_000,
	dispatcher = Dispatchers.IO
)