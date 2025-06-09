# Project Brief: Crypto News MCP Server

**Version**: 1.0
**Date**: 2025-06-10

## 1. Core Mission

To develop a robust, real-time MCP (Model Context Protocol) server that provides comprehensive cryptocurrency news analytics. The server will integrate with the Perplexity API to fetch news data and offer a suite of specialized tools for AI assistants like Cursor.

## 2. Key Objectives

- **Develop a Spring Boot-based MCP server.**
- **Integrate Perplexity API** for real-time news aggregation.
- **Implement a sophisticated sentiment analysis engine** tailored for the crypto domain.
- **Provide a rich set of MCP tools** for detailed crypto analysis.
- **Support multiple transport protocols**: HTTP, SSE, and STDIO.
- **Ensure high performance and reliability** through caching and robust error handling.

## 3. Scope

- **In Scope**:
    - Full implementation of the 10 specified MCP tools.
    - Integration with Perplexity API.
    - Custom sentiment analysis logic.
    - Caching mechanisms for news, sentiment, and analytics.
    - Configuration for API keys and server parameters.
    - Comprehensive unit and integration testing.
    - Documentation for setup and usage.

- **Out of Scope**:
    - A user-facing frontend.
    - User authentication and management (beyond API key handling).
    - Historical data backfilling beyond the scope of recent news analysis. 