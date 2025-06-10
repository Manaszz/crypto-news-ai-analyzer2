package com.cryptonews.mcpserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-test")
@Slf4j
public class ApiTestController {

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.url}")
    private String apiUrl;

    @Value("${perplexity.api.model}")
    private String model;

    @GetMapping("/perplexity-auth")
    public ResponseEntity<?> testPerplexityAuth() {
        log.info("Testing Perplexity API authentication");
        log.info("API Key starts with: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
        log.info("API URL: {}", apiUrl);
        log.info("Model: {}", model);

        WebClient webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();

        try {
            // Simple test request
            Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", "Hello"))
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), 
                             clientResponse -> {
                                 log.error("HTTP Status: {}", clientResponse.statusCode());
                                 return clientResponse.bodyToMono(String.class)
                                         .doOnNext(body -> log.error("Error response body: {}", body))
                                         .map(body -> new RuntimeException("API Error: " + clientResponse.statusCode() + " - " + body));
                             })
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "apiKeyPrefix", apiKey.substring(0, Math.min(10, apiKey.length())) + "...",
                "response", response
            ));

        } catch (Exception e) {
            log.error("Error testing Perplexity API", e);
            return ResponseEntity.ok(Map.of(
                "status", "ERROR",
                "apiKeyPrefix", apiKey.substring(0, Math.min(10, apiKey.length())) + "...",
                "error", e.getMessage()
            ));
        }
    }
} 