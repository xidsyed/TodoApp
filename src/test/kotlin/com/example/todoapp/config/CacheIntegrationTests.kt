package com.example.todoapp.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.*
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import kotlin.test.*

@SpringBootTest

class CacheIntegrationTests @Autowired constructor(
	private val cacheManager: CacheManager,
	private val template: R2dbcEntityTemplate
) {

	@Test
	fun `test cache persistence`(): Unit {
		// write an item

		// item should reflect in database

	}

	@Test
	fun `test cache eviction from db `(): Unit {
		// soft delete
		
	}


	@Test
	fun `writes rejected from db fail`(): Unit {

	}

	@Test
	fun `cache is repopulated at restart `(): Unit {

	}

	@Test
	fun `cache is repopulated post crash `(): Unit {

	}


	@Test
	fun `caches are pruned at restart`(): Unit {
		// soft delete
	}

	@Test
	fun `caches are time-marked on repopulation`(): Unit {

	}

	@Test
	fun `cache puts are async`(): Unit {

	}

	@Test
	fun `cache gets are sync`(): Unit {

	}

	@Test
	fun `scheduled db purges purge cachekvunit `(): Unit {

	}

	@Test
	fun `test write and read failures and timeouts` () : Unit {

	}


}