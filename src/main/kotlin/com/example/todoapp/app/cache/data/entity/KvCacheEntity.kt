package com.example.todoapp.app.cache.data.entity

import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.*
import java.time.OffsetDateTime

/*
// OLD ENTITY

@Table("cache_kv")
data class KvCacheEntity(

	@Column("entry")
	val entry: String,

	@Column("value")
	val value: String,

	@Column("cache_id")
	val cacheId: String,

	@Column("expire_at")
	val expireAt: OffsetDateTime,

	@Column("deleted_at")
	val deletedAt: OffsetDateTime? = null,
)
*/


@Table("cache_kv")
data class KvCacheEntity(

	@Column("id_key")
	val key: String,

	@Column("value")
	val value: String,

	@Column("id_cache")
	val cache: String,

	@Column("expire_at")
	val expireAt: OffsetDateTime,

	@Column("deleted_at")
	val deletedAt: OffsetDateTime? = null,

	@Id
	@Column("id")
	val id: Long? = null,  // Surrogate PK
)