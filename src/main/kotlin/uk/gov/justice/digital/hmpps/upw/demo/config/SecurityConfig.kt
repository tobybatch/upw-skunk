package uk.gov.justice.digital.hmpps.upw.demo.config

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Base64
import kotlin.text.split
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity, @Autowired objectMapper: ObjectMapper): SecurityFilterChain {
        http {
            csrf { disable() }

            authorizeHttpRequests {
                authorize(HttpMethod.GET, "/swagger-ui.html", permitAll)
                authorize(HttpMethod.GET, "/health/**", permitAll)
                authorize(HttpMethod.GET, "/swagger-ui/**", permitAll)
                authorize(HttpMethod.GET, "/public/**", permitAll)
                authorize(HttpMethod.GET, "/probationOnly/**", hasAnyAuthority("ROLE_PROBATION", "ROLE_POM"))
            }

            anonymous { disable() }

            oauth2ResourceServer {
                jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() }

                authenticationEntryPoint = AuthenticationEntryPoint { _, response, _ ->
                    response.apply {
                        status = 401
                        contentType = "application/problem+json"
                        characterEncoding = "UTF-8"

                        writer.write(
                            objectMapper.writeValueAsString(
                                object {
                                    val title = "Unauthenticated"
                                    val status = 401
                                    val detail =
                                        "A valid HMPPS Auth JWT must be supplied via bearer authentication to access this endpoint"
                                },
                            ),
                        )
                    }
                }
            }

            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            addFilterAfter(JwtDebugLoggingFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter::class.java)

        }

        return http.build()
    }
}


class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
        JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val claims = jwt.claims
        val principal = findPrincipal(claims)
        val authorities = extractAuthorities(jwt)
        return AuthAwareAuthenticationToken(jwt, principal, authorities)
    }

    private fun extractAuthSource(claims: Map<String, Any?>): String = claims[CLAIM_AUTH_SOURCE] as String

    private fun findPrincipal(claims: Map<String, Any?>): String = if (claims.containsKey(CLAIM_USERNAME)) {
        claims[CLAIM_USERNAME] as String
    } else if (claims.containsKey(CLAIM_USER_ID)) {
        claims[CLAIM_USER_ID] as String
    } else if (claims.containsKey(CLAIM_CLIENT_ID)) {
        claims[CLAIM_CLIENT_ID] as String
    } else {
        throw RuntimeException("Unable to find a claim to identify Subject by")
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>().apply { addAll(jwtGrantedAuthoritiesConverter.convert(jwt)!!) }
        if (jwt.claims.containsKey(CLAIM_AUTHORITY)) {
            @Suppress("UNCHECKED_CAST")
            val claimAuthorities = when (val claims = jwt.claims[CLAIM_AUTHORITY]) {
                is String -> claims.split(',')
                is Collection<*> -> (claims as Collection<String>).toList()
                else -> emptyList()
            }
            authorities.addAll(claimAuthorities.map(::SimpleGrantedAuthority))
        }
        return authorities.toSet()
    }

    companion object {
        const val CLAIM_USERNAME = "user_name"
        const val CLAIM_USER_ID = "user_id"
        const val CLAIM_AUTH_SOURCE = "auth_source"
        const val CLAIM_CLIENT_ID = "client_id"
        const val CLAIM_AUTHORITY = "authorities"
    }
}

class AuthAwareAuthenticationToken(
    jwt: Jwt,
    private val aPrincipal: String,
    authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {

    private val jwt = jwt

    override fun getPrincipal(): String = aPrincipal

}