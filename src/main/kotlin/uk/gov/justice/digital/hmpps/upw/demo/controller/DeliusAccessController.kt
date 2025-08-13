package uk.gov.justice.digital.hmpps.upw.demo.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.upw.demo.client.ApDeliusContextApiClient
import uk.gov.justice.digital.hmpps.upw.demo.client.ClientResult
import uk.gov.justice.digital.hmpps.upw.demo.client.deliuscontext.StaffDetail
import uk.gov.justice.digital.hmpps.upw.demo.config.AuthAwareAuthenticationToken

@RestController
@RequestMapping("/delius")
class DeliusAccessController() {

    @RequestMapping("/{username}")
    fun getAccess(@PathVariable username: String, apDeliusContextApiClient: ApDeliusContextApiClient): String {
        val authenticatedPrincipal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
        val username = authenticatedPrincipal.name
        val normalisedUsername = username.uppercase()

        val deliusUser: StaffDetail =
            when (val staffUserDetailsResponse = apDeliusContextApiClient.getStaffDetail(normalisedUsername)) {
                is ClientResult.Success<*> -> staffUserDetailsResponse.body as StaffDetail
                is ClientResult.Failure<*> -> staffUserDetailsResponse.throwException()
            }

        return deliusUser.name.deliusName()
    }
}