package com.cryptonews.mcpserver;

import com.cryptonews.mcpserver.controller.SseController;
import com.cryptonews.mcpserver.controller.ToolController;
import com.cryptonews.mcpserver.service.SseService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.function.Function;

import static org.mockito.Mockito.when;

@WebFluxTest
@Import({SseToolIntegrationTest.TestConfig.class, ToolController.class, SseController.class})
public class SseToolIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SseService sseService;

    @Autowired
    private ApplicationContext applicationContext;

    @Configuration
    static class TestConfig {
        @Bean
        public Function<String, Mono<Object>> getMarketSentiment() {
            return symbol -> Mono.just("Positive");
        }
    }

    @Test
    public void testToolExecutionOverSse() {
        // Given
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();
        when(sseService.getSink()).thenReturn(sink);

        // When
        webTestClient.post()
                .uri("/tools/execute")
                .bodyValue(new ToolController.ToolExecutionRequest("getMarketSentiment", "BTC"))
                .exchange()
                .expectStatus().isOk();

        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("getMarketSentiment-result")
                .data("Positive")
                .build());

        // Then
        StepVerifier.create(sink.asFlux())
                .expectNextMatches(event -> event.event().equals("getMarketSentiment-result") && event.data().equals("Positive"))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
} 