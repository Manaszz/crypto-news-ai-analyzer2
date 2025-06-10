# Technical Context: Crypto News MCP Server

## 1. Core Technologies

- **Programming Language**: Java 21
- **Framework**: Spring Boot 3.3.0
- **AI Integration**: Spring AI 1.0.0-M6
- **Build Tool**: Maven
- **Data Source**: Perplexity API

## 2. Development Setup

1.  **Clone the repository.**
2.  **Configure `application.yml`**: The main configuration is in `src/main/resources/`. You must provide your Perplexity API key here under `perplexity.api.key`.
3.  **Configure Test Environment**: A separate configuration for tests is located at `src/test/resources/application.yml`. This file uses dummy keys and properties to allow the application context to load during testing.
4.  **Build the project**: Run `mvn clean install`.
5.  **Run the server**: Run `mvn spring-boot:run` or execute the main application class.

## 3. Key Dependencies (`pom.xml`)

- **Parent**: `spring-boot-starter-parent` (version 3.3.0)
- **Spring AI**:
    - `spring-ai-bom`: Manages all Spring AI dependency versions.
    - `spring-ai-openai-spring-boot-starter`: Provides OpenAI-compatible API integration, used for Perplexity.
    - `spring-ai-mcp-server-spring-boot-starter`: Core dependency for MCP functionality.
- **Web**: `spring-boot-starter-web`, `spring-boot-starter-webflux`
- **Data**: `spring-boot-starter-data-jpa`, `h2`
- **Caching**: `spring-boot-starter-cache`, `caffeine`
- **Testing**: `spring-boot-starter-test`, `reactor-test`, `spring-cloud-starter-contract-stub-runner` (for WireMock)

## 4. Environment Template (`.env.template`)

The project is configured via `application.yml` files, not `.env`.

**Main Configuration (`src/main/resources/application.yml`):**
```yaml
perplexity:
  api:
    key: "your_perplexity_api_key_here"
    url: "https://api.perplexity.ai"
    model: "llama-3-sonar-large-32k-online"
``` 