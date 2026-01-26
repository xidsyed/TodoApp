package com.example.todoapp.app.quiz.converter

import com.example.todoapp.app.quiz.model.view.QuizView
import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.*
import tools.jackson.databind.json.JsonMapper

@WritingConverter
class QuizViewListToJsonConverter(
	private val jsonMapper: JsonMapper
) : Converter<List<QuizView>, Json> {
	override fun convert(source: List<QuizView>): Json {
		return Json.of(jsonMapper.writeValueAsString(source))
	}
}

@ReadingConverter
class JsonToQuizViewJsonConverter(
	private val jsonMapper: JsonMapper
) : Converter<Json, List<QuizView>> {
	override fun convert(source: Json): List<QuizView> {
		val type = jsonMapper.typeFactory
			.constructCollectionType(List::class.java, QuizView::class.java)
		return jsonMapper.readValue(source.asString(), type)
	}
}
