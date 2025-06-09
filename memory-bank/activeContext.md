# Active Context: Crypto News MCP Server

## 1. Current Focus

With the project refactored and foundational services in place, the primary focus is now on implementing the core business logic. The most critical next step is to build the `PerplexityNewsClient` to enable communication with the Perplexity API.

This corresponds to the main implementation part of **Task 1: Perplexity API Integration Service**.

## 2. Next Steps

1.  **Implement `PerplexityNewsClient.java`**:
    - Use Spring's `WebClient` to build the HTTP client.
    - Configure the client to use the `perplexity.api.key` and `perplexity.api.url` from `application.yml`.
    - Implement a method to execute search queries against the Perplexity API.
    - Add robust error handling for API-specific exceptions.
2.  **Integrate Client with `NewsAnalyticsService.java`**:
    - Inject the `PerplexityNewsClient` into the `NewsAnalyticsService`.
    - Replace placeholder logic in the service methods with actual calls to the client.
    - Implement the data transformation logic to map API responses to our internal `NewsItem` model.
3.  **Implement Caching**:
    - Add `@Cacheable` annotations to the relevant methods in `NewsAnalyticsService` to cache results from the Perplexity API.

## 3. Key Decisions & Patterns

- **API Client**: We will use `WebClient` for its non-blocking, reactive capabilities, which is a good fit for an I/O-bound application like this.
- **Separation of Concerns**: The `PerplexityNewsClient` will be responsible for all direct interaction with the API, while the `NewsAnalyticsService` will orchestrate the calls and handle business logic. This keeps the code clean and easier to test.
- **Configuration-driven**: All API-related settings (URL, key) will be externalized to `application.yml`, allowing for easy changes without modifying the code. 