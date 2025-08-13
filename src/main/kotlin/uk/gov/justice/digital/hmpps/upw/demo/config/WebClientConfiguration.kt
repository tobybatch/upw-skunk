package uk.gov.justice.digital.hmpps.upw.demo.config

import io.netty.channel.ChannelOption
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import org.springframework.beans.factory.annotation.Qualifier

data class WebClientConfig(
  val webClient: WebClient,
  val maxRetryAttempts: Long = 1,
  val retryOnReadTimeout: Boolean = false,
)

@SuppressWarnings("LongParameterList")
@Configuration
class WebClientConfiguration(
  @Value("\${services.default.timeout-ms}") private val defaultUpstreamTimeoutMs: Long,
  @Value("\${services.default.max-response-in-memory-size-bytes}") private val defaultMaxResponseInMemorySizeBytes: Int,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Bean
  fun authorizedClientManager(clients: ClientRegistrationRepository): OAuth2AuthorizedClientManager {
    val service: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clients)
    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service)
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()
    manager.setAuthorizedClientProvider(authorizedClientProvider)
    return manager
  }

  @Bean
  @Qualifier("apDeliusContextApiWebClient")
  fun apDeliusContextApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.ap-delius-context-api.base-url}") apDeliusContextApiBaseUrl: String,
  ): WebClientConfig {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("delius-backed-apis")

    return WebClientConfig(
      WebClient.builder()
        .baseUrl(apDeliusContextApiBaseUrl)
        .filter(oauth2Client)
        .clientConnector(
          ReactorClientHttpConnector(
            HttpClient
              .create()
              .responseTimeout(Duration.ofMillis(defaultUpstreamTimeoutMs))
              .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                Duration.ofMillis(defaultUpstreamTimeoutMs).toMillis().toInt()
              ),
          ),
        )
        .exchangeStrategies(
          ExchangeStrategies.builder().codecs {
            it.defaultCodecs().maxInMemorySize(defaultMaxResponseInMemorySizeBytes)
          }.build(),
        )
        .build(),
      retryOnReadTimeout = true,
    )
  }

  @Bean
  @Qualifier("hmppsTierApiWebClient")
  fun hmppsTierApiWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.hmpps-tier.base-url}") hmppsTierApiBaseUrl: String,
    @Value("\${services.hmpps-tier.timeout-ms}") tierApiUpstreamTimeoutMs: Long,
  ): WebClientConfig {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("hmpps-tier")

    return WebClientConfig(
      WebClient.builder()
        .baseUrl(hmppsTierApiBaseUrl)
        .clientConnector(
          ReactorClientHttpConnector(
            HttpClient
              .create()
              .responseTimeout(Duration.ofMillis(tierApiUpstreamTimeoutMs))
              .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                Duration.ofMillis(tierApiUpstreamTimeoutMs).toMillis().toInt()
              ),
          ),
        )
        .filter(oauth2Client)
        .build(),
      retryOnReadTimeout = true,
    )
  }
}
