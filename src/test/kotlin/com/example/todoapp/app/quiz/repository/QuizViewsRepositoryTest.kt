package com.example.todoapp.app.quiz.repository

import com.example.todoapp.NewzDBIntegrationTest
import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.quiz.mapper.*
import com.example.todoapp.app.quiz.model.entity.*
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import com.example.todoapp.common.util.logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*
import kotlin.test.*

class QuizViewsRepositoryTest @Autowired constructor(
	private val quizItemRepo: QuizItemRepository,
	private val quizRepo: QuizRepository,
	private val tagRepo: TagRepository,
	private val quizItemsTagsRepo: ItemsTagsRepository,
	private val quizzesQuizItemsRepo: QuizzesQuizItemsRepository,
	private val channelRepo: ChannelRepository,
	private val viewRepo: QuizViewsRepository,
	private val userRepo: UserRepository,
) : NewzDBIntegrationTest(){

	private val logger = logger()

	private val user1Id = UUID.randomUUID()
	private val user2Id = UUID.randomUUID()

	private lateinit var channelId: UUID
	private lateinit var quiz1Id: UUID
	private lateinit var quiz2Id: UUID
	private lateinit var item1Id: UUID
	private lateinit var item2Id: UUID

	@BeforeTest
	fun setup() = runBlocking {
		// create two users
		val user1 = UserEntity(
			userId = user1Id,
			displayName = "user 1",
			profilePic = "https://picsum.photos/id/1/200/300.jpg",
			role = NewzroomRoleEntity.ADMIN,
			email = "quiztest1@example.com"
		)
		val user2 = UserEntity(
			userId = user2Id,
			displayName = "user 2",
			profilePic = "https://picsum.photos/id/12/200/300.jpg",
			role = NewzroomRoleEntity.WRITER,
			email = "quiztest2@example.com"
		)
		userRepo.saveAll(listOf(user1, user2)).collect()

		// channel
		val channel = ChannelEntity(
			name = "daily",
			description = "Daily quizzes channel",
		)
		val savedChannel = channelRepo.save(channel)
		channelId = savedChannel.id!!

		// tags
		tagRepo.saveAll(
			listOf(
				TagEntity(name = "world"),
				TagEntity(name = "politics")
			)
		).collect()

		// quiz items
		val now = Instant.now()
		val item1 = QuizItemEntity(
			title = "Item One",
			description = "desc1",
			imageId = "img1",
			question = "Who won something?",
			options = arrayOf("A", "B", "C", "D"),
			key = 1,
		)
		val item2 = QuizItemEntity(
			id = null,
			title = "Item Two",
			description = "desc2",
			imageId = "img2",
			question = "What happened in place X?",
			options = arrayOf("W", "X", "Y", "Z"),
			key = 2,
			createdAt = now,
			updatedAt = now,
			deletedAt = null,
		)

		val savedItem1 = quizItemRepo.save(item1)
		val savedItem2 = quizItemRepo.save(item2)
		item1Id = savedItem1.id!!
		item2Id = savedItem2.id!!

		// quizzes
		val quiz1 = QuizEntity(
			id = null,
			title = "Quiz One",
			description = "The first quiz",
			channelId = channelId,
			authorId = user1Id,
			createdAt = now,
			updatedAt = now,
			publishedAt = now,
			deletedAt = null,
		)
		val quiz2 = QuizEntity(
			id = null,
			title = "Quiz Two",
			description = "Another quiz",
			channelId = channelId,
			authorId = user2Id,
			createdAt = now,
			updatedAt = now,
			publishedAt = now,
			deletedAt = null,
		)

		val savedQuiz1 = quizRepo.save(quiz1)
		val savedQuiz2 = quizRepo.save(quiz2)
		quiz1Id = savedQuiz1.id!!
		quiz2Id = savedQuiz2.id!!

		// quizzes_quiz_items join rows (positions)
		val join1 = QuizzesQuizItemsEntity.new(
			quiz1Id, item1Id,
			pos = 1,
		)
		val join2 = QuizzesQuizItemsEntity.new(
			quiz1Id, item2Id,
			2,
		)
		val join3 = QuizzesQuizItemsEntity.new(
			quiz2Id, item2Id,
			1,
		)

		quizzesQuizItemsRepo.saveAll(listOf(join1, join2, join3)).collect()

		// item tags
		val itTag1 = ItemsTagsEntity(
			itemTagId = ItemTagId(item1Id, "world"),
		)
		val itTag2 = ItemsTagsEntity(
			itemTagId = ItemTagId(item2Id, "politics"),
		)
		quizItemsTagsRepo.saveAll(listOf(itTag1, itTag2)).collect()
	}

	@AfterTest
	fun cleanup(): Unit = runBlocking {
		// 1. Delete join rows (quizzes_quiz_items)

		/**
		 * Need to use deleteById, because lack of proper compatibility for Composite Key's in Springboot 4.0
		 * */
		listOf(
			QuizzesQuizItemsId(quiz1Id, item1Id),
			QuizzesQuizItemsId(quiz1Id, item2Id),
			QuizzesQuizItemsId(quiz2Id, item2Id)
		).forEach {
			quizzesQuizItemsRepo.deleteById(it)
		}

		/**
		 * Need to use deleteById, because lack of proper compatibility for Composite Key's in Springboot 4.0
		 * */
		// 2. Delete tag relations (items_tags)
		listOf(
			ItemTagId(item1Id, "world"),
			ItemTagId(item2Id, "politics")
		).forEach {
			quizItemsTagsRepo.deleteById(it)
		}

		// 3. Delete quizzes
		quizRepo.deleteAllById(listOf(quiz1Id, quiz2Id))

		// 4. Delete items
		quizItemRepo.deleteAllById(listOf(item1Id, item2Id))

		// 5. Delete tags created in this test
		tagRepo.deleteAllById(listOf("world", "politics"))

		// 6. Delete channel
		channelRepo.deleteById(channelId)

		// 7. Delete users
		userRepo.deleteAllById(listOf(user1Id, user2Id))
	}

	@Test
	fun `views return expected quiz summary and mapping`() = runBlocking {
		// fetch summaries
		val summaries = viewRepo.findAllQuizzesWithItemSummary().toList()
		// group into DTOs using your mappers
		val summaryDtos = summaries.toQuizItemSummaryDtos()

		// we expect at least quiz1 present (quiz2 may also appear depending on join) - assert presence and counts
		assertTrue(summaryDtos.isNotEmpty())
		val quizOneSummary = summaryDtos.first { it.quiz.title == "Quiz One" }
		assertEquals("Quiz One", quizOneSummary.quiz.title)
		// quiz one had 2 items
		assertEquals(2, quizOneSummary.items.size)
		// item titles match inserted items
		assertTrue(quizOneSummary.items.any { it.title == "Item One" || it.question.contains("Who won") })

		// fetch a single quiz summary by id
		val singleRows = viewRepo.findQuizWithItemSummaryById(quiz1Id).toList()
		val singleDtoList = singleRows.toQuizItemSummaryDtos()
		assertEquals(1, singleDtoList.size)
		val singleDto = singleDtoList.first()
		assertEquals(2, singleDto.items.size)
	}

	@Test
	fun `full item view and tags + quizzes views behave correctly`() = runBlocking {
		// full item view (includes options, key, story)
		val full = viewRepo.findAllQuizWithItem().toList()
		val fullDtos = full.toQuizItemDtos()
		assertTrue(fullDtos.isNotEmpty())
		// find quiz one dto
		val q1 = fullDtos.first { it.quiz.title == "Quiz One" }
		// items should preserve order and have options
		assertEquals(2, q1.items.size)
		assertTrue(q1.items[0].options.isNotEmpty())

		// item tags view
		val tagged = viewRepo.findAllQuizzesWithItemTags().toList()
		val taggedDtos = tagged.toQuizItemTagsDtos()
		assertTrue(taggedDtos.isNotEmpty())
		val q1Tagged = taggedDtos.first { it.quiz.title == "Quiz One" }
		assertEquals(2, q1Tagged.items.size)
		// each ItemWithTagsDto has tags
		assertTrue(q1Tagged.items.any { it.tags.isNotEmpty() })

		// item tags + quizzes (checks the item_quizzes JSON aggregate)
		val tagsQuizzes = viewRepo.findAllQuizzesWithItemTagsQuizzes().toList()
		val tagsQuizzesDtos = tagsQuizzes.toQuizItemTagsQuizzesDtos()
		assertTrue(tagsQuizzesDtos.isNotEmpty())
		val q1TQ = tagsQuizzesDtos.first { it.quiz.title == "Quiz One" }
		// The item that is reused by quiz2 should show item_quizzes containing quiz2 inside its quizzes list
		val item2Wrapper =
			q1TQ.items.first { it.item.title == "Item Two" || it.item.question.contains("What happened") }
		// since Item Two was also part of Quiz Two, item2Wrapper.quizzes should contain at least one entry for Quiz Two
		assertTrue(item2Wrapper.quizzes.any { it.title == "Quiz Two" })
	}
}
