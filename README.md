# Testing

This project uses a robust integration testing strategy centered around the `NewzDBIntegrationTest` base class. This class provides a consistent and efficient way to test database interactions.

## Integration Test Setup

The `NewzDBIntegrationTest` class (`src/test/kotlin/com/example/todoapp/NewzDBIntegrationTest.kt`) is the foundation for all database-related integration tests. It automates the following:

*   **Testcontainers for PostgreSQL:** It spins up a PostgreSQL database in a Docker container using [Testcontainers](https://www.testcontainers.org/). This ensures that tests run in a clean, isolated, and reproducible database environment.
*   **Container Lifecycle:** The PostgreSQL container is configured with `.withReuse(true)`, meaning it is started only once and reused across all local test **indefinetely**, significantly speeding up test execution.
*   **Dynamic Configuration:** It dynamically configures the Spring `R2DBC` to connect to the test container.
*   **Schema Management with Flyway:** On initial startup, it automatically applies all Flyway schema migrations located in `newzdb/supabase/migrations`.
*   **Deterministic Database State:** Before each test method, it completely resets the database to a known baseline.

### Note
In the interest of speeding up local development the container is statically created without a `@Testcontainer` annotation, and is meant to be reused.
To take advantage of this, you must also add `testcontainers.reuse.enable=true` in `~/.testcontainers.properties` file in your **system's home directory**.
Not the project home directory. 

The same test container will remain active and be reused across all test runs. This has the following implications
1. Running cleanup after each test run / method is imperative
2. The container will **never** shutdown automatically, it must be manually terminated. thankfully it should only consume a few (20-30 MBs) in RAM and should idle at 0% CPU.


## Writing Integration Tests

To write an integration test that requires the database, simply extend your test class from `NewzDBIntegrationTest`:

```kotlin
class MyServiceIntegrationTest : NewzDBIntegrationTest() {

    @Autowired
    private lateinit var myRepository: MyRepository

    @Test
    fun `should retrieve saved data`() {
        // ... test logic that interacts with the database
    }
}
```

### Database Reset Mechanism

To ensure test isolation, the database is reset before each test (`@BeforeEach`):

1.  **Truncate Tables:** All tables in the `public` schema (except for `flyway_schema_history`) are truncated using a `TRUNCATE ... CASCADE` command. This is a fast and efficient way to delete all data.
2.  **Apply Baseline Seeds:** After truncation, it re-applies baseline seed data.

### Baseline Seeding

Baseline seeds are SQL files that populate the database with essential data required for the application to function correctly (e.g., default roles, system settings).

*   **Location:** Baseline seed files are located in `newzdb/supabase/seeds/`.
*   **Naming Convention:** They must follow the Flyway repeatable migration format: `R__<description>.sql` (e.g., `R__001_baseline_users.sql`).
*   **Execution:** These seed files are executed by `NewzDBIntegrationTest` before each test, ensuring a consistent starting point.

By following this convention, you guarantee that every test runs against a predictable and clean database state, making tests more reliable and easier to reason about.

# Misc

## Code Snippets to instantiate Admin
```kotlin
// before user created
return Ok(Unit)
// custom access token hook
userRepository.save(
	UserEntity(
		userId = jwt.uuid,
		email = jwt.claims.email,
		displayName = jwt.claims.userMetadata.fullName,
		profilePic = jwt.claims.userMetadata.avatarUrl,
		role = NewzroomRoleEntity.ADMIN,
	)
)
return Ok(jwt.run { copy(claims = claims.copy(appRole = NewzroomRole.ADMIN)) })
```
# Setup

## Setup Secrets
```.yaml

// src/main/kotlin/resources/secrets.yaml
spring:
  r2dbc:
    username: ****
    password: ****

secret:
  webhook:
    sources:
      supabase: whsec_****
  management:
    SWAGGER:
      - username: ****
        password: ****

```