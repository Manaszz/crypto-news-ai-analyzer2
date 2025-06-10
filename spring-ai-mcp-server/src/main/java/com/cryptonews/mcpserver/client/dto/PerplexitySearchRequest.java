package com.cryptonews.mcpserver.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PerplexitySearchRequest(
        String model,
        List<Message> messages,
        @JsonProperty("return_citations") boolean returnCitations,
        @JsonProperty("return_images") boolean returnImages,
        @JsonProperty("search_recency_filter") String searchRecencyFilter
) {
    public record Message(String role, String content) {}
} 