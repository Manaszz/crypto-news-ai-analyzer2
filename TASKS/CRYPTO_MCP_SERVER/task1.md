# Task 1: Perplexity API Integration Service

**Status**: In Progress  
**Priority**: High  
**Assignee**: @developer  
**Created**: 2025-01-26  
**Due Date**: 2025-01-28  
**Dependencies**: Task 0

## Description

Implement a comprehensive service layer for integrating with Perplexity API to fetch cryptocurrency news and perform sentiment analysis. This replaces the local SQLite database approach with real-time API-based news retrieval.

## Sub-tasks

- [ ] Create Perplexity API client service
    - [ ] Implement HTTP client with WebClient
    - [ ] Configure authentication with API key
    - [ ] Add request/response logging for debugging
    - [ ] Implement rate limiting and retry logic
- [x] Design API request/response DTOs
    - [x] Create PerplexitySearchRequest DTO
    - [x] Create PerplexitySearchResponse DTO
    - [x] Create NewsItem DTO for transformed responses
    - [x] Create SentimentAnalysis DTO
- [ ] Implement news search functionality
    - [ ] Search by cryptocurrency keywords
    - [ ] Apply date filters for recent news
    - [ ] Handle domain filtering for crypto-specific sources
    - [ ] Transform API responses to internal format
- [ ] Implement sentiment analysis
    - [ ] Use Perplexity API for sentiment analysis
    - [ ] Parse sentiment scores and classifications
    - [ ] Aggregate sentiment data for tokens
    - [ ] Cache sentiment results to reduce API calls

## Technical Details

### Files to Create/Modify

- `src/main/java/com/cryptonews/mcpserver/service/PerplexityService.java` - Main API integration service
- `src/main/java/com/cryptonews/mcpserver/dto/perplexity/` - DTOs for API communication
- `src/main/java/com/cryptonews/mcpserver/dto/mcp/` - DTOs for MCP responses
- `src/main/java/com/cryptonews/mcpserver/config/PerplexityConfig.java` - Configuration properties
- `src/main/java/com/cryptonews/mcpserver/exception/PerplexityApiException.java` - Custom exception handling

### Architecture Decisions

1. **WebClient**: Use Spring WebFlux WebClient for non-blocking HTTP calls
2. **Rate Limiting**: Implement token bucket algorithm for API rate limits
3. **Caching**: Use Spring Cache with TTL for frequently requested data
4. **Error Handling**: Comprehensive exception handling with fallback strategies

### Dependencies

- Spring Boot Starter WebFlux
- Spring Boot Starter Cache
- Caffeine Cache implementation
- Jackson for JSON processing

## Progress Tracking

**Sub-tasks Completed**: 1/4 (25%)

### Change History

- `2025-01-26 00:00` - Task created
- `2025-06-10 00:00` - Implemented core services, models and configurations. Client is a placeholder.

## Notes

- Perplexity API supports sonar-pro model with 2000 RPM rate limit
- Use search_recency_filter for recent crypto news
- Implement domain filtering for reputable crypto news sources
- Consider implementing circuit breaker pattern for API resilience 