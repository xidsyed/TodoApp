package com.example.todoapp

import com.example.jooq.generated.tables.pojos.UserProfiles
import com.example.jooq.generated.tables.references.USER_PROFILES
import com.example.todoapp.common.util.*
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class ScratchSpringbootTest @Autowired constructor(
	private val jooq: DSLContext,
) : NewzDBIntegrationTest() {

	@Test
	fun `test jooq transactions rollbacks work`() = runTest {
		runCatching {
			jooq.tx {
				val newRecord = newRecord(
					USER_PROFILES,
					UserProfiles(
						id = UUID.randomUUID(),
						email = "joqq@joqq.com",
						role = "writer",
						displayName = "jooq",
						profilePic = "",
					)
				)
				insertInto(USER_PROFILES).set(newRecord).returning().awaitSingle()
				error("oops")
			}
		}

		val userProfiles = jooq.ex {
			selectFrom(USER_PROFILES)
				.awaitAllAs<UserProfiles>()
		}
		assert(userProfiles.isEmpty())
	}


}
