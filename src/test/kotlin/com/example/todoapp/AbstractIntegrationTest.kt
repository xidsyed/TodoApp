package com.example.todoapp

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.*
import java.io.File

@SpringBootTest
@Testcontainers
abstract class AbstractIntegrationTest {

    @Autowired
    lateinit var databaseClient: DatabaseClient

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("todoapp")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("test-init.sql")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { 
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/todoapp" 
            }
            
            registry.add("spring.flyway.url") { 
                postgres.jdbcUrl
            }
        }
    }

    @BeforeEach
    fun setupSeed() {
        // 1. Truncate all tables to ensure a clean slate
        postgres.createConnection("").use { connection ->
            val statement = connection.createStatement()
            statement.execute("DO \$\$ DECLARE r RECORD; BEGIN FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename != 'flyway_schema_history') LOOP EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE'; END LOOP; END \$\$;")

            // 2. Run seed.sql
            val seedFile = File("newz_supabase/supabase/seed.sql")
            if (seedFile.exists()) {
                ScriptUtils.executeSqlScript(connection, FileSystemResource(seedFile))
            }
        }
    }
}
