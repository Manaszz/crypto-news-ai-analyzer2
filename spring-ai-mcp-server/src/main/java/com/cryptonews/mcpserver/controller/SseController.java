package com.cryptonews.mcpserver.controller;

import com.cryptonews.mcpserver.service.SseService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/sse/news")
    public Flux<ServerSentEvent<String>> sse() {
        return sseService.getSink().asFlux();
    }
} 