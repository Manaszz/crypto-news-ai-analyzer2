package com.cryptonews.mcpserver.controller;

import com.cryptonews.mcpserver.service.SseService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RestController
public class ToolController {

    private final ApplicationContext applicationContext;
    private final SseService sseService;

    public ToolController(ApplicationContext applicationContext, SseService sseService) {
        this.applicationContext = applicationContext;
        this.sseService = sseService;
    }

    @PostMapping("/tools/execute")
    public Mono<ResponseEntity<Void>> executeTool(@RequestBody ToolExecutionRequest request) {
        @SuppressWarnings("unchecked")
        Function<String, Mono<?>> function = (Function<String, Mono<?>>) applicationContext.getBean(request.toolName());
        return function.apply(request.toolInput())
                .doOnSuccess(result -> sseService.send(request.toolName() + "-result", result))
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/tools/execute-sync")
    public Mono<?> executeToolSync(@RequestBody ToolExecutionRequest request) {
        @SuppressWarnings("unchecked")
        Function<String, Mono<?>> function = (Function<String, Mono<?>>) applicationContext.getBean(request.toolName());
        return function.apply(request.toolInput());
    }

    public record ToolExecutionRequest(String toolName, String toolInput) {
    }
} 