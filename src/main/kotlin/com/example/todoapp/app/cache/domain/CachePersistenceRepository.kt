package com.example.todoapp.app.cache.domain

import com.example.todoapp.app.cache.data.entity.KvCacheEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CachePersistenceRepository : CoroutineCrudRepository<KvCacheEntity, String> {
    override suspend fun <S : KvCacheEntity> save(entity: S): KvCacheEntity
    override fun <S : KvCacheEntity> saveAll(entities: Iterable<S>): Flow<S>

    @Query("SELECT * FROM cache_kv WHERE id_key = :id AND id_cache = :cache AND deleted_at IS NULL")
    suspend fun findById(id: String, cache: String): KvCacheEntity?

	@Modifying
	@Query("UPDATE cache_kv SET deleted_at = NOW() WHERE id_key = :id AND id_cache = :cache AND deleted_at IS NULL")
    suspend fun deleteById(id: String, cache: String): Long

    @Query("SELECT * FROM cache_kv WHERE id_cache = :cache AND deleted_at IS NULL")
    fun findByCache(cache: String): Flow<KvCacheEntity>

    @Query("SELECT * FROM cache_kv WHERE id_cache = :cache AND expire_at < NOW() AND deleted_at IS NULL")
    fun findByCacheExpired(cache: String): Flow<KvCacheEntity>

    @Modifying
    @Query("UPDATE cache_kv SET deleted_at = NOW() WHERE id_cache = :cache AND expire_at < NOW() AND deleted_at IS NULL")
    suspend fun deleteExpiredByCache(cache: String): Long

    @Modifying
    @Query("DELETE FROM cache_kv WHERE deleted_at IS NOT NULL")
    suspend fun purgeDeleteEntries(): Long


    @Modifying
    @Query("""
		INSERT INTO cache_kv (id_key, id_cache, value, expire_at, deleted_at)
		VALUES (:#{#entity.key}, :#{#entity.cache}, :#{#entity.value}, :#{#entity.expireAt}, :#{#entity.deletedAt})
		ON CONFLICT (id_key, id_cache) DO UPDATE
		SET value = EXCLUDED.value,
			expire_at = EXCLUDED.expire_at,
			deleted_at = EXCLUDED.deleted_at
    """
    )
    suspend fun upsert(entity: KvCacheEntity): Long
}

