package com.example.todoapp

import org.flywaydb.core.Flyway
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
import java.sql.Connection

@SpringBootTest
@Testcontainers
abstract class NewzDBIntegrationTest {

	@Autowired
	lateinit var databaseClient: DatabaseClient

	companion object {

		@Container
		val postgres = PostgreSQLContainer("postgres:15-alpine")
			.withDatabaseName("newzdb")
			.withUsername("postgres")
			.withPassword("postgres")
			.withInitScript("test-init.sql")
			.withReuse(true)

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
