package com.example.todoapp.common.serialize

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.*

val defaultObjectMapper: ObjectMapper = jacksonObjectMapper()
	.registerModule(KotlinModule.Builder().build())     // kotlin support (data classes, nullability, etc)
	.apply {
		 //configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	}

inline fun <reified T> jacksonSerializer(
	mapper: ObjectMapper = defaultObjectMapper
): Serializer<T> {
	// Create a TypeReference<T> so Jackson captures generic info (List<Foo>, Map<..>, etc)
	val typeRef = object : TypeReference<T>() {}
	// Convert TypeReference -> JavaType (Jackson internal type handle)
	val javaType: JavaType = mapper.typeFactory.constructType(typeRef)
	// Instantiate reader/writer for that JavaType once (cheaper to reuse)
	val writer = mapper.writerFor(javaType)
	val reader = mapper.readerFor(javaType)

	return object : Serializer<T> {
		override fun serialize(obj: T): String =
			try {
				writer.writeValueAsString(obj) // uses the preconfigured ObjectWriter
			} catch (ex: Exception) {
				throw SerializationException("serialize failed for ${T::class}", ex)
			}

		override fun deserialize(s: String): T =
			try {
				reader.readValue<T>(s) // uses the preconfigured ObjectReader
			} catch (ex: Exception) {
				throw SerializationException("deserialize failed for ${T::class}", ex)
			}
	}
}