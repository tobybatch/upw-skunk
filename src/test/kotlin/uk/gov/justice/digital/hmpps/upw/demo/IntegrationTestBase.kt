package uk.gov.justice.digital.hmpps.upw.demo

import java.util.UUID
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
class IntegrationTestBase {
    @Autowired
    lateinit var jwtAuthHelper: JwtAuthHelper

    @Autowired
    lateinit var webTestClient: WebTestClient

}