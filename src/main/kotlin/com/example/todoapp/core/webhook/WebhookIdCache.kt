package com.example.todoapp.core.webhook

import com.example.todoapp.core.cache.*
import com.example.todoapp.core.cache.data.PersistedCacheImpl
import com.example.todoapp.core.serializer.createJacksonSerializer
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.time.Duration


@Component
class WebhookIdCache @Autowired constructor(
	cachePersistenceRepository: CachePersistenceRepository,
	jsonMapper: JsonMapper,
) : PersistedCache<String, Boolean> by PersistedCacheImpl(
	persistence = cachePersistenceRepository,
	cacheId = "webhook_cache",
	keySerializer = createJacksonSerializer<String>(jsonMapper),
	valueSerializer = createJacksonSerializer<Boolean>(jsonMapper),
	defaultDuration = Duration.ofSeconds(Webhook.TOLERANCE_IN_SECONDS.toLong()),
	cacheSize = 1_000_000,
	dispatcher = Dispatchers.IO
)