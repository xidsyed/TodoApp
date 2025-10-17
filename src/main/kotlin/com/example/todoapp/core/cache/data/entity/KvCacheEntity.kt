package com.example.todoapp.core.cache.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.*
import java.time.Instant

@Table("cache_kv")
data class KvCacheEntity(

	@Column("id_key")
	val key: String,

	@Column("value")
	val value: String,

	@Column("id_cache")
	val cache: String,

	@Column("expire_at")
	val expireAt: Instant,

	@Column("deleted_at")
	val deletedAt: Instant? = null,

	@Id
	@Column("id")
	val id: Long? = null,  // Surrogate PK
)