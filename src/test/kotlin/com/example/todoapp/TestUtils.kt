package com.example.todoapp

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object TestUtils {
	fun testLog(obj: Any?) {
		val now = Clock.System.now().toLocalDateTime(TimeZone.Companion.UTC).time.toString().substringBefore(".")
		println("$now :: $obj")
	}
}