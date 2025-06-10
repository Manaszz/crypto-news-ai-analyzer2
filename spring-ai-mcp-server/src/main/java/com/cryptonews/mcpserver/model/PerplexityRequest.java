package com.cryptonews.mcpserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record PerplexityRequest(
    String model,
    List<Message> messages,
    @JsonProperty("search_mode") String searchMode,
    @JsonProperty("reasoning_effort") String reasoningEffort,
    Double temperature,
    @JsonProperty("top_p") Double topP,
    @JsonProperty("return_images") Boolean returnImages,
    @JsonProperty("return_related_questions") Boolean returnRelatedQuestions,
    @JsonProperty("top_k") Integer topK,
    Boolean stream,
    @JsonProperty("presence_penalty") Double presencePenalty,
    @JsonProperty("frequency_penalty") Double frequencyPenalty,
    @JsonProperty("web_search_options") Map<String, Object> webSearchOptions
) {
    public record Message(String role, String content) {
    }
    
    // Constructor with defaults for easier usage
    public PerplexityRequest(String model, List<Message> messages) {
        this(model, messages, "web", "medium", 0.2, 0.9, false, false, 0, false, 0.0, 0.0, 
             Map.of("search_context_size", "low"));
    }
} 