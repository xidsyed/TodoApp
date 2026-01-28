package com.example.todoapp

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.*
import org.testcontainers.containers.PostgreSQLContainer
import java.io.File
import java.sql.Connection

/**
 * Base class for integration tests that require a database.
 *
 * This class sets up a PostgreSQL container using Testcontainers and configures the application
 * to use it. It also ensures the database is reset to baseline before each test, ensuring
 * that tests are isolated and run against a known state.
 *
 * ## Test Lifecycle
 *
 * 1.  **Container Startup:** A reusable PostgreSQL container is started once per test suite run.
 * 2.  **Schema Migration:** Flyway migrations are applied once when the container starts to set up the schema.
 * 3.  **Database Reset (Before Each Test):**
 *     - All tables are truncated.
 *     - Baseline seed data is re-applied.
 *
 * ## Usage
 *
 * To use this class, simply extend it in your test class:
 *
 * ```kotlin
 * class MyServiceIntegrationTest : NewzDBIntegrationTest() {
 *
 *     @Autowired
 *     lateinit var myService: MyService
 *
 *     @Test
 *     fun `my service should do something`() {
 *         // ...
 *     }
 * }
 * ```
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

		val postgres = PostgreSQLContainer("postgres:15-alpine")
			.withDatabaseName("newzdb")
			.withUsername("postgres")
			.withPassword("postgres")
			.withReuse(true)


		@BeforeAll
		@JvmStatic
		fun startContainer() {
			postgres.start()
		}

		@JvmStatic
		@DynamicPropertySource
		fun setProperties(registry: DynamicPropertyRegistry) {
			applySchemaAndBaselineMigrations()
			registry.add("spring.r2dbc.username") { "postgres" }
			registry.add("spring.r2dbc.password") { "postgres" }
			registry.add("spring.r2dbc.url") {
				"r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/newzdb"
			}
		}

		/**
		 * Runs ONCE per JVM:
		 * - schema migrations
		 * - baseline (R__) migrations
		 */
		private fun applySchemaAndBaselineMigrations() {
			Flyway.configure()
				.dataSource(postgres.jdbcUrl, "postgres", "postgres")
				.schemas("public", "extensions")
				.defaultSchema("public")
				.locations(
					"filesystem:newzdb/supabase/migrations",
					"filesystem:newzdb/supabase/seeds"
				)
				.sqlMigrationPrefix("")
				.repeatableSqlMigrationPrefix("R")
				.sqlMigrationSeparator("_")
				.baselineOnMigrate(true)
				.load()
				.migrate()
		}
	}

	/**
	 * Resets the database to a clean state before each test.
	 * This is done by truncating all tables and then re-applying baseline seed data.
	 */
	@BeforeEach
	fun resetDatabase() {
		postgres.createConnection("").use { connection ->
			truncateAllTables(connection)
			applyBaselineSeeds(connection)
		}
	}

	/**
	 * Fast, deterministic truncation:
	 * - excludes flyway_schema_history
	 * - uses catalog-driven SQL (no PL/pgSQL DO blocks)
	 */
	private fun truncateAllTables(connection: Connection) {
		val truncateSql = buildString {
			append("TRUNCATE TABLE ")

			connection.metaData.getTables(null, "public", "%", arrayOf("TABLE"))
				.use { rs ->
					val tables = mutableListOf<String>()
					while (rs.next()) {
						val table = rs.getString("TABLE_NAME")
						if (table != "flyway_schema_history") {
							tables += "\"public\".\"$table\""
						}
					}
					append(tables.joinToString(", "))
				}

			append(" CASCADE;")
		}

		connection.createStatement().use { it.execute(truncateSql) }
	}

	/**
	 * Re-applies ALL baseline seed files before each test.
	 * Convention:
	 *   R_###_baseline_*.sql
	 */
	private fun applyBaselineSeeds(connection: Connection) {
		val seedDir = File("newzdb/supabase/seeds")

		if (!seedDir.exists()) return

		seedDir
			.listFiles { _, name ->
				name.matches(Regex("""R_\d+_baseline_.*\.sql"""))
			}
			?.sortedBy { it.name }
			?.forEach { seed ->
				ScriptUtils.executeSqlScript(
					connection,
					FileSystemResource(seed)
				)
			}
	}
}
