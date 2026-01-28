package com.example.todoapp.app.quiz.controller

import com.example.todoapp.AbstractIntegrationTest
import com.example.todoapp.app.auth.roles.data.entity.NewzroomRoleEntity
import com.example.todoapp.app.quiz.domain.service.ChannelValidationService
import com.example.todoapp.app.quiz.mapper.*
import com.example.todoapp.app.quiz.model.dto.*
import com.example.todoapp.app.quiz.model.entity.*
import com.example.todoapp.app.quiz.repository.*
import com.example.todoapp.app.users.UserRepository
import com.example.todoapp.app.users.entity.UserEntity
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.*
import java.time.Instant
import java.util.*
import kotlin.random.Random

@AutoConfigureWebTestClient
class QuizzesControllerTest @Autowired constructor(
	private val context: ApplicationContext,
	private val quizItemRepo: QuizItemRepository,
	private val quizRepo: QuizRepository,
	private val tagRepo: TagRepository,
	private val itemsTagsRepo: ItemsTagsRepository,
	private val quizzesQuizItemsRepo: QuizzesQuizItemsRepository,
	private val channelRepo: ChannelRepository,
	private val userRepo: UserRepository,
	private val viewRepo: QuizViewsRepository,
	private val channelValidationService: ChannelValidationService,
	private val webTestClient: WebTestClient
) : AbstractIntegrationTest() {


	private val savedUser = UserEntity(
		userId = UUID.randomUUID(),
		displayName = "test user",
		email = "controller-test@example.com",
		role = NewzroomRoleEntity.ADMIN
	)
	private lateinit var draftChannel: ChannelEntity
	private lateinit var newzleChannel: ChannelEntity
	private lateinit var savedQuiz: QuizEntity
	private lateinit var savedItem1: QuizItemEntity
	private lateinit var savedItem2: QuizItemEntity

	@BeforeEach
	fun setup(): Unit = runBlocking {
		// Fetch seeded channels
		val channels = channelRepo.findAll().toList()
		draftChannel = channels.first { it.name == "draft" }
		newzleChannel = channels.first { it.name == "newzle" }

		// Setup entities
		userRepo.save(savedUser)

		tagRepo.save(TagEntity("tech"))

		savedItem1 = createAndSaveQuizItem("Item 1?")
		savedItem2 = createAndSaveQuizItem("Item 2?")

		savedQuiz = createAndSaveQuiz("Initial Title 1", draftChannel.id!!, savedUser.userId)

		quizzesQuizItemsRepo.save(QuizzesQuizItemsEntity.new(savedQuiz.id!!, savedItem1.id!!, 1))
	}

	@Test
	fun `updateQuiz - should successfully update title, items, and positions of existing quiz`(): Unit = runBlocking {
		// Arrange: Create a request DTO to update the quiz
		val currentQuizItemTagsDto =
			viewRepo.findQuizWithItemTagsById(savedQuiz.id!!).toList().toSingleQuizItemTagsDto()!!

		val updatedQuizDto = currentQuizItemTagsDto.quiz.copy(title = "Updated Title")
		val updatedItem1Dto = currentQuizItemTagsDto.items.first().copy(tags = listOf("tech")) // Add a tag to item 1
		val importedItemDto = savedItem2.toDto(0)
		val newItemDto = generateQuizItemTagsDto()

		val request = createQuizItemTagsDto(
			quiz = updatedQuizDto, items = listOf(
				ItemTagsDto(importedItemDto, listOf("tech")), updatedItem1Dto, // Keep item 1 (with added tag)
				newItemDto
			)
		)

		// Act & Assert
		webTestClient.put().uri("${QuizzesController.QUIZ_PATH}/${savedQuiz.id}")
			.contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange()
			.expectStatus().isOk.expectBody<QuizItemTagsDto>().consumeWith { response ->
				val result = response.responseBody!!
				assertEquals("Updated Title", result.quiz.title)
				assertEquals(2, result.items.size)
				assertEquals(savedItem2.id, result.items[0].item.id) // Check new position
				assertEquals(savedItem1.id, result.items[1].item.id) // Check new position
				assertTrue(result.items[1].tags.contains("tech"))
			}
	}

	@Test
	fun `updateQuiz - should succeed when publishing a new quiz to newzle channel with valid data`(): Unit =
		runBlocking {
			// Arrange: A quiz for the 'newzle' channel requires at least 9 items.
			val itemsDto = (1..9).map { generateQuizItemTagsDto() }
			val quizDto = createAndSaveQuiz("newzle quiz", newzleChannel.id!!, savedUser.userId).toDto()

			val request = QuizItemTagsDto(quizDto, itemsDto)

			// Act & Assert
			webTestClient.put().uri("${QuizzesController.QUIZ_PATH}/${savedQuiz.id}")
				.contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange()
				.expectStatus().isOk.expectBody<QuizItemTagsDto>().consumeWith { response ->
					val result = response.responseBody!!
					assertEquals(quizDto.title, result.quiz.title)
					assertEquals(newzleChannel.id, result.quiz.channelId)
					assertEquals(9, result.items.size)
				}
		}

	@Test
	fun `updateQuiz - should fail with 400 when path ID does not match body ID`() {
		// Arrange
		val wrongId = UUID.randomUUID()
		val request = createQuizItemTagsDto(
			quiz = savedQuiz.toDto(), items = emptyList()
		)

		// Act & Assert
		webTestClient.put().uri("${QuizzesController.QUIZ_PATH}/$wrongId") // Use wrong ID in path
			.contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest
	}

	// region Helper Functions
	private fun generateQuizItemTagsDto(): ItemTagsDto {
		val id = UUID.randomUUID()
		return ItemTagsDto(
			item = ItemDto(
				id = id,
				title = "title_$id",
				description = "description$id",
				imageId = id.toString(),
				question = "Sample Question",
				options = listOf("A", "B", "C"),
				key = Random.nextInt(0, 3).toShort(),
				story = StoryDto(
					parts = listOf(
						StoryDto.StoryPartDto(
							imageId = "part$id", imageCaption = "caption", brief = "lorem ipsum dolor amet"
						)
					)
				),
				createdAt = Instant.now(),
				version = 0,
				quizItemRelationVersion = 0
			),
			tags = listOf("tech"),
		)
	}

	private fun createQuizItemTagsDto(
		quiz: QuizDto, items: List<ItemTagsDto>
	): QuizItemTagsDto {
		return QuizItemTagsDto(quiz = quiz, items = items)
	}

	private fun createItemTagsDto(
		item: ItemDto,
		tags: List<String> = emptyList(),
	): ItemTagsDto {
		return ItemTagsDto(item = item, tags = tags)
	}

	private suspend fun createAndSaveQuizItem(question: String): QuizItemEntity {
		return quizItemRepo.save(QuizItemEntity(question = question, options = arrayOf("A"), key = 0))
	}

	private suspend fun createAndSaveQuiz(title: String, channelId: UUID, authorId: UUID): QuizEntity {
		return quizRepo.save(
			QuizEntity(
				title = title, channelId = channelId, authorId = authorId, publishedAt = Instant.now()
			)
		)
	}
	// endregion
}
