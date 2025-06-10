# Crypto News MCP Server

This project is a Spring Boot-based MCP (Model Context Protocol) server that provides real-time cryptocurrency news analytics. It integrates with the Perplexity API to fetch news data and offers a suite of specialized tools for AI assistants.

## Features

- **Real-time News:** Fetches the latest cryptocurrency news from the Perplexity API.
- **Advanced Analytics:** Provides tools for sentiment analysis, market trends, and more.
- **Multiple Transports:** Supports STDIO, HTTP, and SSE transport layers for flexible integration.
- **Extensible:** Easily extendable with new tools and data sources.
- **Containerized:** Comes with a Docker and Docker Compose setup for easy deployment.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- Docker and Docker Compose

### Building and Running

You can build and run the application using Docker Compose.

1.  **Create a `.env` file:**
    Create a `.env` file in the `spring-ai-mcp-server` directory and add your Perplexity API key:
    ```
    PERPLEXITY_API_KEY=your_perplexity_api_key_here
    ```

2.  **Run with Docker Compose:**
    ```bash
    cd spring-ai-mcp-server
    docker-compose up --build
    ```
    The application will be available at `http://localhost:8080`.

## Configuration

The application is configured via `application.yml` and environment-specific profiles (`application-dev.yml`, `application-prod.yml`). The active profile can be set using the `SPRING_PROFILES_ACTIVE` environment variable.

## Available MCP Tools

The server exposes the following tools via the MCP protocol:

- `getLatestCryptoNews(symbol)`: Get the latest news for a cryptocurrency.
- `getTrendingCryptos()`: Get a list of trending cryptocurrencies.
- `getMarketSentiment(symbol)`: Get the market sentiment for a cryptocurrency.
- `getCryptoAnalysis(symbol)`: Get a detailed analysis for a cryptocurrency.
- `getHistoricalPriceData(symbol)`: Get historical price data.
- `getSocialMediaBuzz(symbol)`: Get the latest social media buzz.
- `compareCryptoPerformance(symbol1, symbol2)`: Compare the performance of two cryptocurrencies.
- `getUpcomingIcos()`: Get information on upcoming ICOs.
- `getFearAndGreedIndex()`: Get the current Fear and Greed Index.
- `getTopPerformers()`: Get the top-performing cryptocurrencies.

## License

Private repository - All rights reserved. 