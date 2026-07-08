package com.example.todoapp

import com.example.todoapp.common.util.logger
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.*
import org.testcontainers.postgresql.PostgreSQLContainer
import java.io.File
import java.sql.Connection

/**
 * Base class for database-backed integration tests.
 *
 * Lifecycle:
 * 1. Start (reused) Postgres container once per JVM
 * 2. Apply schema migrations once
 * 3. Before each test:
 *    - truncate all tables
 *    - re-apply baseline seed data
 */
@SpringBootTest(
	properties = [
		"springdoc.api-docs.enabled=false",
		"springdoc.swagger-ui.enabled=false"
	]
)
abstract class NewzDBIntegrationTest {

	@Autowired
	lateinit var databaseClient: DatabaseClient

	companion object {

		private val logger = logger()

		// ----------------------------
		// Container
		// ----------------------------

		private val postgres = PostgreSQLContainer("postgres:15.15-alpine")
			.withDatabaseName("newzdb")
			.withUsername("postgres")
			.withPassword("postgres")
			.withReuse(true)

		@BeforeAll
		@JvmStatic
		fun beforeAll() {
			logger.info("Starting Postgres container")
			postgres.start()

			applySchemaMigrations()
			resetDatabaseToBaseline()
		}

		@AfterAll
		@JvmStatic
		fun afterAll() {
			withConnection { truncateAllTables(it) }
		}

		@DynamicPropertySource
		@JvmStatic
		fun registerProperties(registry: DynamicPropertyRegistry) {
			registry.add("spring.r2dbc.username") { "postgres" }
			registry.add("spring.r2dbc.password") { "postgres" }
			registry.add("spring.r2dbc.url") {
				"r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/newzdb"
			}
		}

		// ----------------------------
		// Flyway (schema only)
		// ----------------------------

		private fun applySchemaMigrations() {
			logger.info("Applying Flyway schema migrations")

			Flyway.configure()
				.dataSource(postgres.jdbcUrl, "postgres", "postgres")
				.defaultSchema("public")
				.locations("filesystem:newzdb/supabase/migrations")
				.sqlMigrationPrefix("")
				.repeatableSqlMigrationPrefix("R")
				.sqlMigrationSeparator("_")
				.validateMigrationNaming(true)
				.baselineOnMigrate(true)
				.load()
				.migrate()
		}

		// ----------------------------
		// Database utilities
		// ----------------------------

		private fun withConnection(block: (Connection) -> Unit) {
			postgres.createConnection("").use(block)
		}

		private fun truncateAllTables(connection: Connection) {
			val tables = connection.metaData
				.getTables(null, "public", "%", arrayOf("TABLE"))
				.use { rs ->
					buildList {
						while (rs.next()) {
							val table = rs.getString("TABLE_NAME")
							if (table != "flyway_schema_history") {
								add("\"public\".\"$table\"")
							}
						}
					}
				}

			if (tables.isEmpty()) return

			val sql = "TRUNCATE TABLE ${tables.joinToString(", ")} CASCADE;"
			connection.createStatement().use { it.execute(sql) }
		}

		private fun applyBaselineSeeds(connection: Connection) {
			val seedDir = File("newzdb/supabase/seeds")
			if (!seedDir.exists()) return

			seedDir.listFiles { _, name ->
				name.matches(Regex("""R_\d+_baseline_.*\.sql"""))
			}
				?.sortedBy { it.name }
				?.forEach { seed ->
					logger.info("→ ${seed.name}")
					ScriptUtils.executeSqlScript(
						connection,
						FileSystemResource(seed)
					)
				}
		}

		private fun resetDatabaseToBaseline() {
			logger.info("Resetting database to baseline")
			withConnection {
				truncateAllTables(it)
				applyBaselineSeeds(it)
			}
		}
	}

	// ----------------------------
	// Per-test reset
	// ----------------------------

	@BeforeEach
	fun beforeEach() {
		resetDatabaseToBaseline()
	}

}
