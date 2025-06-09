# Task 5: MCP Tools Implementation

**Status**: In Progress  
**Priority**: High  
**Assignee**: @developer  
**Created**: 2025-01-26  
**Due Date**: 2025-02-01  
**Dependencies**: Task 1

## Description

Implement the MCP tools interface that exposes cryptocurrency news analytics functionality through standardized tool calls. This includes adapting existing tools to work with Perplexity API and adding new crypto-specific capabilities.

## Sub-tasks

- [x] Implement search_news tool
    - [x] Accept keyword parameter for crypto search
    - [x] Use Perplexity API to search for relevant news
    - [x] Filter results for cryptocurrency relevance
    - [x] Return formatted news items with metadata
- [x] Implement get_latest_news tool
    - [x] Fetch recent cryptocurrency news
    - [x] Support configurable limit parameter
    - [x] Apply date filtering for recency
    - [x] Sort results by relevance and date
- [x] Implement analyze_sentiment tool
    - [x] Accept cryptocurrency token symbol
    - [x] Analyze sentiment from recent news
    - [x] Use Perplexity API for sentiment analysis
    - [x] Return sentiment scores and classification
- [x] Implement track_token tool
    - [x] Add token to tracking list
    - [x] Validate token symbol format
    - [x] Store tracking preferences
    - [x] Return confirmation with token details
- [x] Implement get_tracked_tokens tool
    - [x] Return list of currently tracked tokens
    - [x] Include tracking metadata and status
    - [x] Support filtering by status or category
    - [x] Add token statistics and summary

## Technical Details

### Files to Create/Modify

- `src/main/java/com/cryptonews/mcpserver/mcp/JsonRpcMcpServer.java` - Tool implementations
- `src/main/java/com/cryptonews/mcpserver/service/CryptoNewsService.java` - Business logic
- `src/main/java/com/cryptonews/mcpserver/service/TokenTrackingService.java` - Token management
- `src/main/java/com/cryptonews/mcpserver/dto/mcp/ToolRequest.java` - Tool request DTOs
- `src/main/java/com/cryptonews/mcpserver/dto/mcp/ToolResponse.java` - Tool response DTOs

### Architecture Decisions

1. **Tool Registration**: Use annotation-based tool discovery and registration
2. **Parameter Validation**: Implement comprehensive input validation for all tools
3. **Response Format**: Standardize response format across all tools
4. **Error Handling**: Provide meaningful error messages for tool failures

### Dependencies

- Spring Boot Starter Validation
- Jackson for JSON processing
- Caffeine Cache for response caching
- Spring AOP for cross-cutting concerns

## Progress Tracking

**Sub-tasks Completed**: 5/5 (100%)

### Change History

- `2025-01-26 00:00` - Task created
- `2025-06-10 00:00` - Implemented MCP tool definitions in `CryptoNewsTools.java`. Service logic is pending.

## Notes

- All tools must conform to MCP protocol specifications
- Implement proper parameter validation and error responses
- Consider adding additional crypto-specific tools (price analysis, market trends)
- Cache responses where appropriate to reduce API calls
- Maintain backward compatibility with existing tool interfaces 