package com.example.todoapp.core.cache.data

import com.example.todoapp.core.cache.CachePersistenceRepository
import com.example.todoapp.core.cache.data.entity.KvCacheEntity
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import java.time.OffsetDateTime
import kotlin.test.*

@SpringBootTest
class CachePersistenceRepositoryIntegrationTests @Autowired constructor(
    private val cacheRepo: CachePersistenceRepository,
    private val dbClient: DatabaseClient
) {


    @AfterEach
    fun cleanup(): Unit = runBlocking {
        dbClient.sql("TRUNCATE TABLE cache_kv").then().block()
    }

    @Test
    fun `findById should not return soft-deleted entity`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cacheId = "cache1"
        val e = KvCacheEntity(key = "k1", value = "v1",cache = cacheId, expireAt = now.plusHours(1).toInstant(), deletedAt = null)
        cacheRepo.save(e)

        val loaded = cacheRepo.findById("k1", cacheId)
        assertNotNull(loaded)

        // Soft delete the entity by updating it directly in the DB
        cacheRepo.deleteById("k1", cacheId)

        val notFound = cacheRepo.findById("k1", cacheId)
        assertNull(notFound)
    }

    @Test
    fun `deleteById should soft-delete a row`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cacheId = "cache1"
        val e = KvCacheEntity(key = "k1", value = "v1",cache = cacheId, expireAt = now.plusHours(1).toInstant())
        cacheRepo.save(e)

        var loaded = cacheRepo.findById("k1", cacheId)
        assertNotNull(loaded)

        val deletedCount = cacheRepo.deleteById("k1", cacheId)
        assertEquals(1, deletedCount)

        // A second delete should not affect any row
        val deletedCount2 = cacheRepo.deleteById("k1", cacheId)
        assertEquals(0, deletedCount2)


        loaded = cacheRepo.findById("k1", cacheId)
        assertNull(loaded)

        val dbRow = dbClient.sql("SELECT * FROM cache_kv WHERE id_key = 'k1'").fetch().one().block()!!
        assertNotNull(dbRow["deleted_at"])
    }

    @Test
    fun `findByCacheIdExpired should return only expired non-deleted entries for the given cacheId`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cache1 = "cache1"

        val dummyEntries = listOf(
            KvCacheEntity("expired_1", "old", cache1, now.minusHours(1).toInstant()),
            KvCacheEntity("expired_2_deleted", "old_deleted", cache1, now.minusDays(1).toInstant(), now.minusDays(1).toInstant()),
            KvCacheEntity("fresh_1", "new", cache1, now.plusHours(1).toInstant())
        )

        cacheRepo.saveAll(dummyEntries).toList()

        val expiredList = cacheRepo.findByCacheExpired(cache1).toList()
        assertEquals(1, expiredList.size)
        assertEquals("expired_1", expiredList.first().key)
    }

    @Test
    fun `deleteExpiredByCacheId should soft delete expired rows for a given cacheId and return count`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cache1 = "cache1"
        val entities = listOf(
            KvCacheEntity("e1", "v", cache1, now.minusMinutes(10).toInstant()),
            KvCacheEntity("f1", "v", cache1, now.plusHours(1).toInstant())
        )

        cacheRepo.saveAll(entities).toList()

        val deletedCount = cacheRepo.deleteExpiredByCache(cache1)
        assertEquals(1, deletedCount)

        val e1 = dbClient.sql("SELECT * FROM cache_kv WHERE id_key = 'e1'").fetch().one().block()!!
        assertNotNull(e1["deleted_at"])

        val f1 = cacheRepo.findById("f1", cache1)
        assertNotNull(f1)
        assertNull(f1.deletedAt)
    }

    @Test
    fun `purgeDeleteEntries should remove rows with deleted_at not null`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cacheId = "cache1"
        val toPurge = KvCacheEntity("d1", "v", cacheId, now.plusHours(1).toInstant(), now.minusDays(1).toInstant())
        val keep = KvCacheEntity("k1", "v", cacheId, now.plusHours(1).toInstant(), null)

        cacheRepo.saveAll(listOf(toPurge, keep)).toList()

        val purgedCount = cacheRepo.purgeDeleteEntries()
        assertEquals(1, purgedCount)

        assertNull(cacheRepo.findById("d1", cacheId))
        assertNotNull(cacheRepo.findById("k1", cacheId))
    }


    @Test
    fun `findByCacheId should return all non-deleted entries for a given cacheId`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cache1 = "cache1"

        val dummyEntries = listOf(
            KvCacheEntity("key1", "val1", cache1, now.plusHours(1).toInstant()),
            KvCacheEntity("key2_deleted", "val2", cache1, now.plusHours(1).toInstant(), now.minusDays(1).toInstant())
        )

        cacheRepo.saveAll(dummyEntries).toList()

        val cache1Entries = cacheRepo.findByCache(cache1).toList()
        assertEquals(1, cache1Entries.size)
        assertEquals("key1", cache1Entries.first().key)
    }

    @Test
    fun `findByCache should return all non-deleted entries for a given cache`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val entities = listOf(
            KvCacheEntity("k1", "v1", "c1", now.plusHours(1).toInstant()),
            KvCacheEntity("k2", "v2", "c2", now.plusHours(1).toInstant()),
            KvCacheEntity("k3_deleted", "v3", "c1", now.plusHours(1).toInstant(), now.minusDays(1).toInstant())
        )
        cacheRepo.saveAll(entities).toList()

        val cache1List = cacheRepo.findByCache("c1").toList()
		val cache2List = cacheRepo.findByCache("c2").toList()
		val allEntries = cache1List + cache2List
        assertEquals(2, allEntries.size)
        assertTrue(allEntries.any { it.key == "k1" })
        assertTrue(allEntries.any { it.key == "k2" })
    }

    @Test
    fun `upsert should insert or update a cache entry`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val cacheId = "upsert_cache"
        val key1 = "key_insert"
        val key2 = "key_update"
        val key3 = "key_revive"

        // 1. Test insert
        val toInsert = KvCacheEntity(key1, "v1", cacheId, now.plusHours(1).toInstant())
        cacheRepo.upsert(toInsert)
        val inserted = cacheRepo.findById(key1, cacheId)
        assertNotNull(inserted)
        assertEquals("v1", inserted.value)

        // 2. Test update
        val original = KvCacheEntity(key2, "v_orig", cacheId, now.plusHours(1).toInstant())
        cacheRepo.save(original)
        val toUpdate = KvCacheEntity(key2, "v_updated", cacheId, now.plusDays(1).toInstant())
        cacheRepo.upsert(toUpdate)
        val updated = cacheRepo.findById(key2, cacheId)
        assertNotNull(updated)
        assertEquals("v_updated", updated.value)
        assertTrue(updated.expireAt.isAfter(now.plusHours(2).toInstant()))

        // 3. Test reviving a soft-deleted entry
        val toDelete = KvCacheEntity(key3, "v_initial", cacheId, now.plusHours(1).toInstant())
        cacheRepo.save(toDelete)
        cacheRepo.deleteById(key3, cacheId)
        assertNull(cacheRepo.findById(key3, cacheId))

        val toRevive = KvCacheEntity(key3, "v_revived", cacheId, now.plusDays(1).toInstant())
        cacheRepo.upsert(toRevive)
        val revived = cacheRepo.findById(key3, cacheId)
        assertNotNull(revived)
        assertEquals("v_revived", revived.value)
        assertNull(revived.deletedAt)
    }
}
