package uk.gov.justice.digital.hmpps.upw.demo.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@Component
class JwtDebugLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtDebugLoggingFilter::class.java)

    @Throws(ServletException::class, java.io.IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val auth = SecurityContextHolder.getContext().authentication

        if (auth is JwtAuthenticationToken) {
            val jwt = auth.token
            log.info("JWT subject: {}", jwt.subject)
            log.info("JWT claims: {}", jwt.claims)
            log.info("Authorities: {}", auth.authorities.map { it.authority })
        }

        filterChain.doFilter(request, response)
    }
}
