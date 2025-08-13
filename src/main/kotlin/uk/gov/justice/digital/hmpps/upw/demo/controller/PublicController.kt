package uk.gov.justice.digital.hmpps.upw.demo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public")
class PublicController {

    @GetMapping("/hello")
    fun sayHello(): Greeting {
        return Greeting("Hello from HMPPS!")
    }
}
