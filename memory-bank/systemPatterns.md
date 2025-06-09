# System Patterns: Crypto News MCP Server

## 1. System Architecture

```mermaid
graph TD
    subgraph "Clients"
        A1[AI Assistant/Cursor]
        A2[Developer/Direct API]
    end

    subgraph "Transport Layer"
        T1[HTTP Transport]
        T2[SSE Transport]
        T3[STDIO Transport]
    end

    subgraph "Application Core"
        subgraph "MCP Server (Spring AI)"
            McpCore[JsonRpcMcpServer]
        end
        subgraph "Service Layer"
            NewsService[NewsAnalyticsService]
            SentimentService[SentimentAnalyzer]
        end
        subgraph "Client Layer"
            ApiClient[PerplexityNewsClient]
        end
    end

    subgraph "Data & Caching"
        Cache[Multi-Level Caffeine Cache]
        DB[H2 Database for News Cache]
    end
    
    subgraph "External Services"
        Perplexity[Perplexity API]
    end

    A1 --> T1 & T2 & T3
    A2 --> T1
    
    T1 & T2 & T3 --> McpCore
    McpCore --> NewsService
    
    NewsService --> SentimentService
    NewsService --> ApiClient
    NewsService --> Cache
    NewsService --> DB

    ApiClient --> Perplexity
```

## 2. Key Design Patterns

- **Service-Oriented Architecture**: The application is divided into distinct services (`NewsAnalyticsService`, `SentimentAnalyzer`, `PerplexityNewsClient`), each with a clear responsibility.
- **Dependency Injection**: Spring framework is used extensively to manage dependencies between components.
- **Repository Pattern**: A `NewsItemRepository` will be used to abstract the data access layer, even if it's just for caching to an H2 database.
- **Function Callbacks**: Spring AI's `FunctionCallback` and `FunctionCallbackWrapper` are the primary mechanism for exposing business logic as tools to the MCP clients.
- **Builder Pattern**: The `CryptoAnalytics` model object will use a builder for clean and readable construction.
- **Caching**: Multi-level caching (`news-cache`, `sentiment-cache`, `analytics-cache`) is used to improve performance and reduce API calls. 