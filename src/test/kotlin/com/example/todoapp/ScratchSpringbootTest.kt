package com.example.todoapp

import com.example.jooq.generated.tables.pojos.UserProfiles
import com.example.jooq.generated.tables.references.USER_PROFILES
import com.example.todoapp.app.users.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class ScratchSpringbootTest @Autowired constructor(
	private val dsl: DSLContext,
	private val userRepository: UserRepository
) : NewzDBIntegrationTest() {

	@Test
	fun `simple jooq query`() = runBlocking {
		val newUser = UserProfiles(
			id = UUID.randomUUID(),
			email = "some@some.com",
			role = "writer",
			displayName = "some",
			profilePic = "some@some.com",
		)

		val inserted = dsl
			.insertInto(USER_PROFILES)
			.set(dsl.newRecord(USER_PROFILES, newUser))
			.returning()
			.awaitSingle()
			.into(UserProfiles::class.java)


		println("inserted: $inserted")

		val profilesResult = dsl.selectFrom(USER_PROFILES).asFlow().map { it.into(UserProfiles::class.java) }.toList()
		println("profilesResult: $profilesResult")
	}
}