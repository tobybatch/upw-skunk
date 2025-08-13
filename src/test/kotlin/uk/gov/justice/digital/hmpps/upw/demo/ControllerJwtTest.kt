package uk.gov.justice.digital.hmpps.upw.demo

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ControllerJwtTest : IntegrationTestBase() {


    @Nested
    inner class NoJwt {

        @Test
        fun `fails on an unlisted route without a JWT`() {
            webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isForbidden
        }

        @Test
        fun `succeeds on an public route without a JWT`() {
            webTestClient.get()
                .uri("/public/hello")
                .exchange()
                .expectStatus()
                .isOk
        }

        @Test
        fun `fails on an protected route without a JWT`() {
            webTestClient.get()
                .uri("/probationOnly/probationOnly")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }
    }

    @Nested
    inner class GoodJwt {

        @Test
        fun `access a route using a global security context`() {
            val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
                subject = "username",
                authSource = "delius",
                roles = listOf("ROLE_PROBATION"),
            )
            webTestClient.get()
                .uri("/probationOnly/getWithGlobalSecurity")
                .header("Authorization", "Bearer $jwt")
                .exchange()
                .expectStatus()
                .isOk
        }

        @Test
        fun `access a route using a pre-auth security context`() {
            val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
                subject = "username",
                authSource = "delius",
                roles = listOf("ROLE_POM"),
            )
            webTestClient.get()
                .uri("/probationOnly/getWithLocalSecurity")
                .header("Authorization", "Bearer $jwt")
                .exchange()
                .expectStatus()
                .isOk
        }

        @Test
        fun `fail to access a route using a pre-auth security context`() {
            val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
                subject = "username",
                authSource = "delius",
                roles = listOf("ROLE_PROBATION"),
            )
            webTestClient.get()
                .uri("/probationOnly/getWithLocalSecurity")
                .header("Authorization", "Bearer $jwt")
                .exchange()
                .expectStatus()
                .isForbidden
        }
    }
}