package com.example.todoapp.common.exception

import jdk.internal.joptsimple.internal.Messages.message


open class CommonNotFoundException(
	val entity: String,
	val id: String,
	extra: String? = null,
	override val cause: Throwable? = null
) : RuntimeException("$entity with id $id not found. ${extra ?: ""}", cause)