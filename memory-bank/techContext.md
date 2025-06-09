# Technical Context: Crypto News MCP Server

## 1. Core Technologies

- **Programming Language**: Java 21
- **Framework**: Spring Boot 3.4.4
- **AI Integration**: Spring AI 1.0.0-M6 (for MCP Server)
- **Build Tool**: Maven 3.6+
- **Data Source**: Perplexity API

## 2. Development Setup

1.  **Clone the repository.**
2.  **Configure `application.yml`**: The primary configuration file is located in `src/main/resources/`.
3.  **Set Environment Variables**: The Perplexity API key must be provided. The recommended approach is to set an environment variable `PERPLEXITY_API_KEY`.
4.  **Build the project**: Run `mvn clean install`.
5.  **Run the server**: Run `mvn spring-boot:run` or execute the main application class.

## 3. Key Dependencies

- `spring-boot-starter-web`: For HTTP transport.
- `spring-ai-mcp-server-spring-boot-starter`: Core dependency for MCP functionality.
- `spring-boot-starter-test`: For JUnit 5 and Mockito testing.
- `caffeine`: For in-memory caching.
- `spring-boot-starter-data-jpa` & `h2`: For a simple database to cache news items.

## 4. Environment Template (`.env.template`)

```
# Perplexity API Configuration
PERPLEXITY_API_KEY="your_perplexity_api_key_here"
``` 