# Task 0: Project Setup and Architecture Analysis

**Status**: Planned  
**Priority**: High  
**Assignee**: @developer  
**Created**: 2025-01-26  
**Due Date**: 2025-01-27  
**Dependencies**: None

## Description

Analyze the existing Spring Boot MCP server codebase, understand its architecture, and set up the development environment for implementing cryptocurrency news analytics with Perplexity API integration.

## Sub-tasks

- [ ] Analyze existing codebase structure and patterns
    - [ ] Review JsonRpcMcpServer implementation
    - [ ] Understand HTTP and SSE transport mechanisms
    - [ ] Analyze NewsService and TrackedTokenService interfaces
- [ ] Study MCP protocol requirements
    - [ ] Review MCP v1.0 and v2025-03-26 specifications
    - [ ] Understand JSON-RPC 2.0 implementation details
    - [ ] Analyze tool registration and invocation patterns
- [ ] Design Perplexity API integration architecture
    - [ ] Study Perplexity API documentation and endpoints
    - [ ] Design service layer for API integration
    - [ ] Plan data transformation from API responses to MCP tools
- [ ] Plan STDIO transport implementation
    - [ ] Research STDIO transport requirements for MCP
    - [ ] Design input/output handling mechanisms
    - [ ] Plan process lifecycle management

## Technical Details

### Files to Create/Modify

- `src/main/java/com/example/service/PerplexityService.java` - New service for API integration
- `src/main/java/com/example/config/PerplexityConfig.java` - Configuration for API
- `src/main/java/com/example/controller/McpStdioController.java` - STDIO transport
- `src/main/resources/application.yml` - Configuration updates

### Architecture Decisions

1. **Perplexity Integration**: Use HTTP client with proper error handling and rate limiting
2. **Configuration Management**: Externalize API keys and endpoints
3. **STDIO Transport**: Implement as separate controller with process I/O handling
4. **Data Mapping**: Create DTOs for API responses and MCP tool responses

### Dependencies

- Spring Boot WebFlux for reactive HTTP client
- Jackson for JSON processing
- Spring Configuration Processor for type-safe configuration

## Progress Tracking

**Sub-tasks Completed**: 0/4 (0%)

### Change History

- `2025-01-26 00:00` - Task created

## Notes

- Need to maintain compatibility with existing HTTP/SSE transports
- Perplexity API has rate limits that need to be respected
- STDIO transport is crucial for integration with AI assistants like Cursor
- Consider implementing caching to reduce API calls 