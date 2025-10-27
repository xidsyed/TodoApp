package com.example.todoapp.common.controller

import com.example.todoapp.app.auth.roles.data.model.NewzroomRole.*
import com.example.todoapp.test.WithMockJwt
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
class RolesControllerTest @Autowired constructor(
    private val client: WebTestClient
) {

    @Test
    @WithMockJwt(role = ADMIN)
    fun `rolesAdmin should return 200 for ADMIN role`() {
        client.get().uri("/roles/admin")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockJwt(role = WRITER)
    fun `rolesAdmin should return 403 for WRITER role`() {
        client.get().uri("/roles/admin")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockJwt(role = ADMIN)
    fun `rolesValidRole should return 200 for ADMIN role`() {
        client.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockJwt(role = WRITER)
    fun `rolesValidRole should return 200 for WRITER role`() {
        client.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockJwt(role = NONE)
    fun `rolesValidRole should return 403 for NONE role`() {
        client.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockJwt(role = ADMIN)
    fun `should return role for ADMIN`() {
        client.get().uri("/roles/current_role")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .isEqualTo(mapOf("role" to "ADMIN"))
    }

    @Test
    @WithMockJwt(role = WRITER)
    fun `should return role for WRITER`() {
        client.get().uri("/roles/current_role")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .isEqualTo(mapOf("role" to "WRITER"))
    }

    @Test
    @WithMockJwt(role = NONE)
    fun `should return role for NONE`() {
        client.get().uri("/roles/current_role")
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .isEqualTo(mapOf("role" to "NONE"))
    }

    @Test
    @WithMockJwt
    fun `authenticatedOnly should return 200 for authenticated user`() {
        client.get().uri("/roles/authenticated_only")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("authenticated")
    }

    @Test
    fun `authenticatedOnly should return 401 for unauthenticated user`() {
        client.get().uri("/roles/authenticated_only")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `rolesValidRole should return 401 for unauthenticated user`() {
        client.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    @WithMockJwt
    fun `rolesValidRole should return 200 for authenticated user with no role`() {
        client.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockJwt(subject = "test-subject-id")
    fun `rolesAdminId should return the subject from the JWT`() {
        val expectedSubject = "test-subject-id"

        client.get().uri("/roles/admin/id")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo(expectedSubject)
    }
}
