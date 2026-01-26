package com.example.todoapp.app.quiz.repository

import com.example.todoapp.app.quiz.model.view.*
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*


const val QUIZ_WITH_ITEM_SUMMARY_CTE = """
	WITH quiz_with_item_summary_view AS (
		SELECT
		  q.id AS quiz_id,
		  q.title AS quiz_title,
		  q.description AS quiz_description,
		  q.channel_id AS quiz_channel_id,
		  q.author_id AS quiz_author_id,
		  q.created_at AS quiz_created_at,
		  q.published_at AS quiz_published_at,
		  q.version AS quiz_version,
		  
		  qqi.pos AS item_pos,
		  qqi.version					  AS quiz_item_relation_version,
	
		  
		  qi.id AS item_id,
		  qi.title AS item_title,
		  qi.question AS item_question,
		  qi.image_id AS item_image_id,
		  qi.version AS item_version,
		  qi.created_at AS item_created_at
		  
		FROM public.quizzes q
		JOIN public.quizzes_quiz_items qqi ON qqi.quiz_id = q.id
		JOIN public.quiz_items qi ON qi.deleted_at IS NULL AND qi.id = qqi.item_id
		
		WHERE q.deleted_at IS NULL
	)
"""

const val QUIZ_WITH_ITEM_CTE = """
	WITH quiz_with_item_view AS (
		SELECT
		q.id AS quiz_id,
		q.title AS quiz_title,
		q.description AS quiz_description,
		q.channel_id AS quiz_channel_id,
		q.author_id AS quiz_author_id,
		q.created_at AS quiz_created_at,
	  	q.published_at AS quiz_published_at,
	  	q.version AS quiz_version,
	  
		qqi.pos AS item_pos,
		qqi.version					  AS quiz_item_relation_version,
		
		qi.id AS item_id,
		qi.title AS item_title,
		qi.description AS item_description,
		qi.image_id AS item_image_id,
		qi.question AS item_question,
		qi.options AS item_options,
		qi.key AS item_key,
		qi.story AS item_story,
		qi.created_at AS item_created_at,
		qi.version AS item_version
		
		FROM public.quizzes q
		JOIN public.quizzes_quiz_items qqi ON qqi.quiz_id = q.id
		JOIN public.quiz_items qi ON qi.deleted_at IS NULL AND qi.id = qqi.item_id
		
		WHERE q.deleted_at IS NULL
	)

"""

const val QUIZ_WITH_ITEM_TAGS_CTE = """
WITH quiz_with_item_tags_view AS (
  SELECT
    q.id                           AS quiz_id,
    q.title                        AS quiz_title,
    q.description                  AS quiz_description,
    q.channel_id                   AS quiz_channel_id,
    q.author_id                    AS quiz_author_id,
    q.created_at                   AS quiz_created_at,
    q.published_at                 AS quiz_published_at,
	q.version					   AS quiz_version,

    qqi.pos                        AS item_pos,
	qqi.version					  AS quiz_item_relation_version,

    qi.id                          AS item_id,
    qi.title                       AS item_title,
    qi.description                 AS item_description,
    qi.image_id                    AS item_image_id,
    qi.question                    AS item_question,
    qi.options                     AS item_options,
    qi.key                         AS item_key,
    qi.story                       AS item_story,
	qi.created_at 				   AS item_created_at,

    qi.version                     AS item_version,

    COALESCE(array_agg(DISTINCT it.tag_name) FILTER (WHERE it.tag_name IS NOT NULL), ARRAY[]::text[]) AS item_tags

  FROM public.quizzes q
  JOIN public.quizzes_quiz_items qqi ON qqi.quiz_id = q.id
  JOIN public.quiz_items qi ON qi.deleted_at IS NULL AND qi.id = qqi.item_id
  LEFT JOIN public.items_tags it ON it.item_id = qi.id
  WHERE q.deleted_at IS NULL
  GROUP BY
    q.id, q.title, q.description, q.channel_id, q.author_id, q.created_at, q.published_at, q.version,
    qqi.pos, qqi.version,
    qi.id, qi.title, qi.description, qi.image_id, qi.question, qi.options, qi.key, qi.story, qi.version
)
"""

const val QUIZ_WITH_ITEM_TAGS_QUIZZES_CTE = """
WITH quiz_with_item_tags_quizzes_view AS (
  SELECT
    q.id                          AS quiz_id,
    q.title                       AS quiz_title,
    q.description                 AS quiz_description,
    q.channel_id                  AS quiz_channel_id,
    q.author_id                   AS quiz_author_id,
    q.created_at                  AS quiz_created_at,
    q.published_at                AS quiz_published_at,
    q.version                     AS quiz_version,

    qqi.pos                       AS item_pos,
	qqi.version					  AS quiz_item_relation_version,

    qi.id                         AS item_id,
    qi.title                      AS item_title,
    qi.description                AS item_description,
    qi.image_id                   AS item_image_id,
    qi.question                   AS item_question,
    qi.options                    AS item_options,
    qi.key                        AS item_key,
    qi.story                      AS item_story,
	qi.created_at 				   AS item_created_at,

    qi.version                    AS item_version,

    COALESCE(array_agg(DISTINCT it.tag_name) FILTER (WHERE it.tag_name IS NOT NULL), ARRAY[]::text[]) AS item_tags,

    COALESCE((
      SELECT jsonb_agg(jsonb_build_object(
        'quiz_id', q2.id,
        'quiz_title', q2.title,
		'quiz_description', q2.description,
        'quiz_channel_id', q2.channel_id,
		'quiz_author_id', q2.author_id,
		'quiz_created_at', q2.created_at,
        'quiz_published_at', q2.published_at,
        'version', q2.version
      ))
      FROM public.quizzes_quiz_items qq2
      JOIN public.quizzes q2 ON q2.id = qq2.quiz_id
      WHERE qi.deleted_at IS NULL AND qq2.item_id = qi.id AND q2.deleted_at IS NULL
    ), '[]'::jsonb) AS item_quizzes

  FROM public.quizzes q
  JOIN public.quizzes_quiz_items qqi ON qqi.quiz_id = q.id
  JOIN public.quiz_items qi ON qi.deleted_at IS NULL AND qi.id = qqi.item_id
  LEFT JOIN public.items_tags it ON it.item_id = qi.id
  WHERE q.deleted_at IS NULL
  GROUP BY
    q.id, q.title, q.description, q.channel_id, q.author_id, q.created_at, q.published_at, q.version,
    qqi.pos, qqi.version,
    qi.id, qi.title, qi.description, qi.image_id, qi.question, qi.options, qi.key, qi.story, qi.version
)
"""


@Repository
interface QuizViewsRepository : CoroutineCrudRepository<QuizItemSummaryView, UUID> {

	@Query(QUIZ_WITH_ITEM_SUMMARY_CTE + "SELECT * FROM quiz_with_item_summary_view WHERE quiz_id = :quizId")
	suspend fun findQuizWithItemSummaryById(quizId: UUID): Flow<QuizItemSummaryView>

	@Query(QUIZ_WITH_ITEM_SUMMARY_CTE + "SELECT * FROM quiz_with_item_summary_view ORDER BY quiz_created_at")
	suspend fun findAllQuizzesWithItemSummary(): Flow<QuizItemSummaryView>

	@Query(QUIZ_WITH_ITEM_CTE + "SELECT * FROM quiz_with_item_view ORDER BY quiz_created_at")
	suspend fun findAllQuizWithItem(): Flow<QuizItemView>

	@Query(QUIZ_WITH_ITEM_CTE + "SELECT * FROM quiz_with_item_view WHERE quiz_id = :quizId")
	suspend fun findQuizWithItemById(quizId: UUID): Flow<QuizItemView>


	@Query(QUIZ_WITH_ITEM_TAGS_CTE + "SELECT * FROM quiz_with_item_tags_view ORDER BY quiz_created_at")
	suspend fun findAllQuizzesWithItemTags(): Flow<QuizItemTagsView>

	@Query(QUIZ_WITH_ITEM_TAGS_CTE + "SELECT * FROM quiz_with_item_tags_view WHERE quiz_id = :quizId")
	suspend fun findQuizWithItemTagsById(quizId: UUID): Flow<QuizItemTagsView>


	@Query(QUIZ_WITH_ITEM_TAGS_QUIZZES_CTE + "SELECT * FROM quiz_with_item_tags_quizzes_view ORDER BY quiz_created_at")
	suspend fun findAllQuizzesWithItemTagsQuizzes(): Flow<QuizItemTagsQuizzesView>

	@Query(QUIZ_WITH_ITEM_TAGS_QUIZZES_CTE + "SELECT * FROM quiz_with_item_tags_quizzes_view WHERE quiz_id = :quizId")
	suspend fun findQuizWithItemTagsQuizzes(quizId: UUID): Flow<QuizItemTagsQuizzesView>
}
