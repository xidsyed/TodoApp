# Project Overview

This is the backend for a news quiz application and the internal cms (wrongfully named todoapplication) built with Kotlin and Spring Boot. It provides a RESTful API for managing tasks, invitations, and users. The application is designed to be reactive, using Spring WebFlux and R2DBC for non-blocking, asynchronous operations. However even though it is reactive the code does not explicitly interact with webflux reactive primitives instead uses relying solely on the kotlin coroutines integration with the reactor project in all places possible.

## Key Technologies

*   **Backend:** Kotlin, Spring Boot, Spring WebFlux, R2DBC
*   **Database:** PostgreSQL
*   **Authentication:** Spring Security with JWT
*   **Build:** Gradle
*   **Containerization:** Docker

## Architecture

The application follows a typical Spring Boot project structure.

*   `src/main/kotlin`: Contains the main application source code.
    *   `com.example.todoapp`: The root package.
        *   `app`: Contains the core application logic, separated by features (e.g., `invitation`, `users`).
        *   `common`: Contains common components like controllers, configuration, models, entities, exception handling, and mappers. anything that doesnt belong to a feature.
        *   `core`: Contains core functionalities like caching and webhooks.
*   `src/main/resources`: Contains application configuration files (`application.yaml`).
*   `src/test/kotlin`: Contains unit and integration tests.
*   `todoapp-api`: Contains Bruno files for API testing and documentation.

# Building and Running

## Prerequisites

*   Java 24
*   Gradle
*   Docker (for running the database)

## Running the Application

1.  **Start the database:**

    ```bash
    docker-compose up -d
    ```

2.  **Run the application:**

    ```bash
    ./gradlew bootRun
    ```

The application will be available at `http://localhost:8080`.

## Running Tests

To run the tests, execute the following command:

```bash
./gradlew test
```

# Development Conventions

*   **Reactive Programming:** The application uses Kotlin coroutines and Spring WebFlux for asynchronous programming.
*   **API Documentation:** The API is documented using Bruno files in the `todoapp-api` directory.
*   **Configuration:** Application configuration is managed in the `application.yaml` file.
*   **Security:** The application uses Spring Security with JWT for authentication and authorization.

# Feature-level Directory Structure

For each new feature, the following directory structure is recommended:

```
src/main/kotlin/com/example/todoapp/app/<feature-name>
├── controller
│   └── <Feature>Controller.kt
├── entity
│   └── <Feature>Entity.kt
├── mapper
│   └── <Feature>Mapper.kt
├── model
│   ├── <Feature>Dto.kt
│   ├── Create<Feature>Request.kt
│   └── Update<Feature>Request.kt
├── repository
│   └── <Feature>Repository.kt
└── service
    └── <Feature>Service.kt
```

*   **controller:** Contains the REST controller for the feature.
*   **entity:** Contains the database entity for the feature.
*   **mapper:** Contains mappers for converting between entities and DTOs.
*   **model:** Contains the data transfer objects (DTOs) for the feature.
*   **repository:** Contains the repository for accessing the feature's data in the database.
*   **service:** Contains the business logic for the feature.
