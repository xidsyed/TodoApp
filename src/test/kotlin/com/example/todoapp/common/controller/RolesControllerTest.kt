package com.example.todoapp.common.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.*


@SpringBootTest
@AutoConfigureWebTestClient
class RolesControllerTest(
	@Autowired
	private val webTestClient: WebTestClient
) {

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `rolesAdmin should return 200 for ADMIN role`() {
        webTestClient.get().uri("/roles/admin")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockUser(roles = ["WRITER"])
    fun `rolesAdmin should return 403 for WRITER role`() {
        webTestClient.get().uri("/roles/admin")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `rolesValidRole should return 200 for ADMIN role`() {
        webTestClient.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockUser(roles = ["WRITER"])
    fun `rolesValidRole should return 200 for WRITER role`() {
        webTestClient.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @WithMockUser(roles = ["NONE"])
    fun `rolesValidRole should return 403 for NONE role`() {
        webTestClient.get().uri("/roles/valid_role")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should return role for ADMIN`() {
        webTestClient.get().uri("/roles/current_role")
            .exchange()
            .expectStatus().isOk
            .expectBody<Map<String, String>>()
            .isEqualTo(mapOf("role" to "ADMIN"))
    }

    @Test
    @WithMockUser(roles = ["WRITER"])
    fun `should return role for WRITER`() {
        webTestClient.get().uri("/roles/current_role")
            .exchange()
            .expectStatus().isOk
            .expectBody<Map<String, String>>()
            .isEqualTo(mapOf("role" to "WRITER"))
    }

    @Test
    @WithMockUser(roles = ["NONE"])
    fun `should return role for NONE`() {
        webTestClient.get().uri("/roles/current_role")
            .exchange()
            .expectStatus().isOk
            .expectBody<Map<String, String>>()
            .isEqualTo(mapOf("role" to "NONE"))
    }
}