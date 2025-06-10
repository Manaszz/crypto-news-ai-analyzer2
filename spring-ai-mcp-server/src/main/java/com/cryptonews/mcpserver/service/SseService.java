package com.cryptonews.mcpserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class SseService {

    private final Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final ObjectMapper objectMapper;

    public SseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void send(String event, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            ServerSentEvent<String> sse = ServerSentEvent.<String>builder()
                    .event(event)
                    .data(jsonData)
                    .build();
            sink.tryEmitNext(sse);
            log.info("Sent SSE event '{}' with data: {}", event, jsonData);
        } catch (Exception e) {
            log.error("Error sending SSE event", e);
        }
    }

    public Sinks.Many<ServerSentEvent<String>> getSink() {
        return sink;
    }
} 
