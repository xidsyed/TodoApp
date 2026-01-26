package com.example.todoapp.app.quiz.converter

import com.example.todoapp.app.quiz.model.view.StoryView
import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.*
import tools.jackson.databind.json.JsonMapper

@WritingConverter
class StoryToJsonConverter(
	private val jsonMapper: JsonMapper, // or JsonMapper
) : Converter<StoryView, Json> {

	override fun convert(source: StoryView): Json {
		val json = jsonMapper.writeValueAsString(source)
		return Json.of(json)   // THIS tells the driver "this is json/jsonb"
	}
}

@ReadingConverter
class JsonToStoryConverter(
	private val jsonMapper: JsonMapper,
) : Converter<Json, StoryView> {

	override fun convert(source: Json): StoryView {
		return jsonMapper.readValue(source.asString(), StoryView::class.java)
	}
}
