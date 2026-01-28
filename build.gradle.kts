plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
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
		mavenBom("org.testcontainers:testcontainers-bom:1.20.0")
	}
}

dependencies {
	// springboot webflux
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// database
	runtimeOnly("org.postgresql:postgresql")
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
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	// flywaydb : only for applying migrations to testcontainers
	testImplementation("org.flywaydb:flyway-core")
	testImplementation("org.flywaydb:flyway-database-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-jdbc")	// needed for flywaydb


	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("org.springframework.security:spring-security-test")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property", "-Xcontext-parameters")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
