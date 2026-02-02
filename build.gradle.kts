plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.jooq.jooq-codegen-gradle") version "3.19.29"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:2.0.3")
	}
}

dependencies {
	// springboot webflux
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// jooq
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.jooq:jooq-kotlin:3.19.29")
	implementation("org.jooq:jooq-kotlin-coroutines:3.19.29")
	jooqCodegen("org.postgresql:postgresql")

	// database
	implementation("org.postgresql:r2dbc-postgresql")


	// Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	// Validation for DTOs
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// jackson
	implementation("tools.jackson.core:jackson-databind:3.0.0")
	implementation("tools.jackson.module:jackson-module-kotlin:3.0.0")
	//  -- v2.20 for springdoc specifically
	implementation(platform("com.fasterxml.jackson:jackson-bom:2.20.0"))
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")


	// caching
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("com.github.ben-manes.caffeine:caffeine:3.2.2")

	// kotlin - spring
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
	implementation("com.michael-bull.kotlin-result:kotlin-result:2.1.0")

	// konform - schema validation
	implementation("io.konform:konform-jvm:0.11.0")

	// springdoc
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.0")

	// devtools
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// annotations
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	// testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-webtestclient")

	// testcontainers
	testImplementation("org.testcontainers:testcontainers-postgresql")

	// flywaydb : only for applying migrations to testcontainers
	testImplementation("org.flywaydb:flyway-core")
	testRuntimeOnly("org.postgresql:postgresql") 	// jdbc driver
	testImplementation("org.flywaydb:flyway-database-postgresql")

	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("org.springframework.security:spring-security-test")
}



buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.postgresql:postgresql:42.7.8")
		classpath("org.flywaydb:flyway-core:11.20.3")
		classpath("org.flywaydb:flyway-database-postgresql:11.20.3")
		classpath("org.testcontainers:postgresql:1.21.4")
		// spins up a new test container if one matching the incoming jdbc url is not already available
		classpath("org.testcontainers:jdbc:1.21.4")
	}
}

// TC_DAEMON=true ensures that the test container stays up after flyway migration
// and across runs. Ryuk kills test container when the Gradle daemon shuts down.
val postgresTcJdbcUrl = "jdbc:tc:postgresql:15.15-alpine:///newzdb?TC_DAEMON=true"
val migrationsPath = "newzdb/supabase/migrations"

jooq {
	configuration {
		jdbc {
			driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
			url = postgresTcJdbcUrl
			user = "postgres"
			password = "postgres"
		}

		generator {
			name = "org.jooq.codegen.KotlinGenerator"

			database {
				name = "org.jooq.meta.postgres.PostgresDatabase"
				inputSchema = "public"

				// Optional but recommended
				excludes = "flyway_schema_history"
			}

			generate {
				records = true
				immutablePojos = true
				fluentSetters = false
				kotlinNotNullPojoAttributes = true
				kotlinNotNullRecordAttributes = true
				jooqVersionReference = false
			}

			target {
				packageName = "com.example.jooq.generated"
				directory = "${layout.buildDirectory.get()}/generated-src/jooq"
			}
		}
	}
}

tasks.named("jooqCodegen") {
	doFirst {
		println("Running Flyway migrations for jOOQ codegen…")

		org.flywaydb.core.Flyway
			.configure()
			.dataSource(
				postgresTcJdbcUrl,
				"postgres",
				"postgres"
			)
			.sqlMigrationPrefix("")
			.sqlMigrationSeparator("_")
			.validateMigrationNaming(true)
			.baselineOnMigrate(true)
			.locations("filesystem:${migrationsPath}")
			.load()
			.migrate()
	}
	inputs.files(fileTree(migrationsPath))
	outputs.dir(layout.buildDirectory.dir("generated-src/jooq"))
}

tasks.named("compileKotlin") {
	dependsOn("jooqCodegen")
}

sourceSets {
	main {
		kotlin.srcDir(layout.buildDirectory.dir("generated-src/jooq"))
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property", "-Xcontext-parameters")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
