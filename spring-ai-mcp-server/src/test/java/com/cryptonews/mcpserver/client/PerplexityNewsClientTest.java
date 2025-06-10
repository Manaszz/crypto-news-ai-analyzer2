package com.cryptonews.mcpserver.client;

import com.cryptonews.mcpserver.client.dto.PerplexitySearchResponse;
import com.cryptonews.mcpserver.model.NewsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PerplexityNewsClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PerplexityNewsClient perplexityNewsClient;

    @BeforeEach
    public void setup() {
        perplexityNewsClient = new PerplexityNewsClient(webClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSearchNews() {
        // Given
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        PerplexitySearchResponse mockResponse = new PerplexitySearchResponse(
                List.of(new PerplexitySearchResponse.Choice(new PerplexitySearchResponse.Message("assistant", "title\nsource\nurl")))
        );
        when(responseSpec.bodyToMono(PerplexitySearchResponse.class)).thenReturn(Mono.just(mockResponse));

        // When
        Mono<NewsResponse> result = perplexityNewsClient.searchNews("test query");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assert response.title().equals("title");
                    assert response.source().equals("source");
                    assert response.url().equals("url");
                })
                .verifyComplete();
    }
} 