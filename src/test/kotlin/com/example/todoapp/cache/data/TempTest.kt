package com.example.todoapp.cache.data

import com.example.todoapp.app.cache.domain.CachePersistenceRepository
import com.example.todoapp.app.cache.data.entity.KvCacheEntity
import com.example.todoapp.config.TestUtils.testLog
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.OffsetDateTime
import kotlin.test.Test

@SpringBootTest
class TempTest @Autowired constructor(
	private val cacheRepo: CachePersistenceRepository,
	private val dbClient: DatabaseClient
) {


	@AfterEach
	fun cleanup(): Unit = runBlocking {
		dbClient.sql("TRUNCATE TABLE cache_kv").then().block()
	}


	@Test
	fun `does it save` () : Unit = runBlocking{
		cacheRepo.save(KvCacheEntity(
			cache= "cache1",
			key= "key",
			value = "value",
			expireAt = OffsetDateTime.now(),
		))

		testLog(cacheRepo.findAll("cache1").toList())
	}
}
