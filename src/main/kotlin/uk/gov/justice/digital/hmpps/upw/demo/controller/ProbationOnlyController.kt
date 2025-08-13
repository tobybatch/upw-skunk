package uk.gov.justice.digital.hmpps.upw.demo.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/probationOnly")
class ProbationOnlyController {

    @GetMapping("/getWithGlobalSecurity")
    fun getWithGlobalSecurity(): Greeting {
        return Greeting("Hello, You are a probation user!")
    }

    @PreAuthorize("hasRole('ROLE_POM')")
    @GetMapping("/getWithLocalSecurity")
    fun getWithLocalSecurity(): Greeting {
        return Greeting("Hello, You are a probation user")
    }
}
