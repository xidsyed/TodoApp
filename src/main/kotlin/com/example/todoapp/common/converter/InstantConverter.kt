package com.example.todoapp.common.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.*
import java.time.*
import java.time.temporal.ChronoUnit

@WritingConverter
class InstantToOffsetDateTimeConverter : Converter<Instant, OffsetDateTime> {
	override fun convert(source: Instant): OffsetDateTime? {
		// Convert to OffsetDateTime in UTC and truncate to micros
		return OffsetDateTime.ofInstant(source, ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
	}
}

@ReadingConverter
class OffsetDateTimeToInstantConverter : Converter<OffsetDateTime, Instant> {
	override fun convert(source: OffsetDateTime): Instant? {
		// Convert to Instant and truncate to micros to match PG precision
		return source.toInstant().truncatedTo(ChronoUnit.MICROS)
	}
}
