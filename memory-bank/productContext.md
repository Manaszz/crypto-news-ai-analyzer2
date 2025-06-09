# Product Context: Crypto News MCP Server

## 1. Problem Statement

AI assistants and developers require timely, accurate, and nuanced information about the cryptocurrency market to make informed decisions. Accessing and interpreting the vast amount of news data is a significant challenge. Existing tools are often generic and lack the specific context of the volatile crypto market.

## 2. Solution

This MCP server acts as a specialized data and analytics bridge. It empowers AI assistants by providing them with a set of powerful tools (`FunctionCallback`s) to:
- Fetch real-time, relevant news from a trusted source (Perplexity API).
- Analyze the sentiment of news with a crypto-specific lens.
- Identify market-moving events and trends.
- Compare different cryptocurrencies based on a rich set of data points.

## 3. User Experience Goals

- **For the AI Assistant (e.g., Cursor)**: Seamless and intuitive access to powerful analytics tools. The assistant should be able to answer complex user queries about the crypto market by simply calling the provided functions.
- **For the Developer**: A server that is easy to set up, configure, and extend. The developer experience should be straightforward, with clear documentation and a logical project structure.
- **For the End-User (via the AI)**: Receive fast, accurate, and insightful answers to their cryptocurrency-related questions. 