package com.project.ai.global.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class OpenAiConfig {
    @Bean
    fun openAiWebClient(
        @Value("\${openai.base-url}") baseUrl: String,
        @Value("\${openai.api-key}") apiKey: String,
    ): WebClient {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(60))

        val exchangeStrategies =
            ExchangeStrategies.builder()
                .codecs { it.defaultCodecs().maxInMemorySize(1024 * 1024) }
                .build()

        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .build()
    }
}
