package com.example.todoapp.core.serializer

import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.JavaType
import tools.jackson.databind.json.JsonMapper



@Autowired
inline fun <reified T> jacksonSerializer(
	mapper: JsonMapper
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
				throw SerializationException("serializer failed for ${T::class}", ex)
			}

		override fun deserialize(s: String): T =
			try {
				reader.readValue<T>(s) // uses the preconfigured ObjectReader
			} catch (ex: Exception) {
				throw SerializationException("deserialize failed for ${T::class}", ex)
			}
	}
}