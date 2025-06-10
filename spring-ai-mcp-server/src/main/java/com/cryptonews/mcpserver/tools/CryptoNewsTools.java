package com.cryptonews.mcpserver.tools;

import com.cryptonews.mcpserver.model.CryptoAnalytics;
import com.cryptonews.mcpserver.model.NewsItem;
import com.cryptonews.mcpserver.service.NewsAnalyticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP инструменты для анализа криптовалютных новостей.
 * Предоставляет набор функций для Claude Desktop и других MCP клиентов.
 */
@Component
@Slf4j
public class CryptoNewsTools {

    private final NewsAnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CryptoNewsTools(NewsAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Получает последние новости по криптовалюте.
     */
    @Tool(name = "getLatestCryptoNews", description = "Retrieves the latest news articles for a specified cryptocurrency with sentiment analysis")
    public String getLatestCryptoNews(String cryptocurrency, Integer maxArticles) {
        try {
            log.info("Getting latest news for: {} (max: {})", cryptocurrency, maxArticles);
            List<NewsItem> news = analyticsService.getLatestCryptoNews(cryptocurrency, maxArticles != null ? maxArticles : 10);
            return objectMapper.writeValueAsString(news);
        } catch (JsonProcessingException e) {
            log.error("Error serializing latest news response", e);
            return "{\"error\": \"Failed to retrieve latest news\"}";
        } catch (Exception e) {
            log.error("Error getting latest news", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Выполняет комплексный анализ криптовалюты.
     */
    @Tool(name = "analyzeCryptocurrency", description = "Performs comprehensive analysis of cryptocurrency including sentiment analysis, trends, and market insights over a specified time period")
    public String analyzeCryptocurrency(String cryptocurrency, String timeRange) {
        try {
            log.info("Analyzing cryptocurrency: {} over {}", cryptocurrency, timeRange);
            CryptoAnalytics analytics = analyticsService.analyzeCryptocurrency(cryptocurrency, timeRange != null ? timeRange : "24 hours");
            return objectMapper.writeValueAsString(analytics);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cryptocurrency analysis response", e);
            return "{\"error\": \"Failed to analyze cryptocurrency\"}";
        } catch (Exception e) {
            log.error("Error analyzing cryptocurrency", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Получает настроение рынка за период.
     */
    @Tool(name = "getMarketSentiment", description = "Analyzes overall market sentiment for a cryptocurrency over a specified time range")
    public String getMarketSentiment(String cryptocurrency, String timeRange) {
        try {
            log.info("Getting market sentiment for: {} over {}", cryptocurrency, timeRange);
            return analyticsService.getMarketSentiment(cryptocurrency, timeRange != null ? timeRange : "24 hours");
        } catch (Exception e) {
            log.error("Error getting market sentiment", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Сравнивает криптовалюты.
     */
    @Tool(name = "compareCryptocurrencies", description = "Compares multiple cryptocurrencies based on recent market performance, news sentiment, and key developments")
    public String compareCryptocurrencies(String cryptocurrencies) {
        try {
            log.info("Comparing cryptocurrencies: {}", cryptocurrencies);
            List<String> cryptoList = List.of(cryptocurrencies.split(","));
            return analyticsService.compareCryptocurrencies(cryptoList);
        } catch (Exception e) {
            log.error("Error comparing cryptocurrencies", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Получает позитивные новости.
     */
    @Tool(name = "getPositiveNews", description = "Retrieves news articles with positive sentiment for a specified cryptocurrency")
    public String getPositiveNews(String cryptocurrency, Integer limit) {
        try {
            log.info("Getting positive news for: {} (limit: {})", cryptocurrency, limit);
            List<NewsItem> news = analyticsService.getPositiveNews(cryptocurrency, limit != null ? limit : 10);
            return objectMapper.writeValueAsString(news);
        } catch (JsonProcessingException e) {
            log.error("Error serializing positive news response", e);
            return "{\"error\": \"Failed to retrieve positive news\"}";
        } catch (Exception e) {
            log.error("Error getting positive news", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Получает негативные новости.
     */
    @Tool(name = "getNegativeNews", description = "Retrieves news articles with negative sentiment for a specified cryptocurrency")
    public String getNegativeNews(String cryptocurrency, Integer limit) {
        try {
            log.info("Getting negative news for: {} (limit: {})", cryptocurrency, limit);
            List<NewsItem> news = analyticsService.getNegativeNews(cryptocurrency, limit != null ? limit : 10);
            return objectMapper.writeValueAsString(news);
        } catch (JsonProcessingException e) {
            log.error("Error serializing negative news response", e);
            return "{\"error\": \"Failed to retrieve negative news\"}";
        } catch (Exception e) {
            log.error("Error getting negative news", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Получает прогноз трендов.
     */
    @Tool(name = "getTrendForecast", description = "Provides short-term trend forecast for a cryptocurrency based on recent news and market analysis")
    public String getTrendForecast(String cryptocurrency) {
        try {
            log.info("Getting trend forecast for: {}", cryptocurrency);
            return analyticsService.getTrendForecast(cryptocurrency);
        } catch (Exception e) {
            log.error("Error getting trend forecast", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Ищет новости по ключевым словам.
     */
    @Tool(name = "searchCryptoNews", description = "Searches for cryptocurrency news articles containing specific keywords or topics")
    public String searchCryptoNews(String cryptocurrency, String keywords) {
        try {
            log.info("Searching crypto news for: {} with keywords: {}", cryptocurrency, keywords);
            List<NewsItem> news = analyticsService.searchCryptoNews(cryptocurrency, keywords);
            return objectMapper.writeValueAsString(news);
        } catch (JsonProcessingException e) {
            log.error("Error serializing search news response", e);
            return "{\"error\": \"Failed to search crypto news\"}";
        } catch (Exception e) {
            log.error("Error searching crypto news", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Получает события, влияющие на рынок.
     */
    @Tool(name = "getMarketMovingEvents", description = "Identifies recent significant events and news that have impacted or are likely to impact the cryptocurrency market")
    public String getMarketMovingEvents(String cryptocurrency) {
        try {
            log.info("Getting market moving events for: {}", cryptocurrency);
            return analyticsService.getMarketMovingEvents(cryptocurrency);
        } catch (Exception e) {
            log.error("Error getting market moving events", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Анализирует корреляцию настроения и цены.
     */
    @Tool(name = "analyzeSentimentPriceCorrelation", description = "Analyzes the correlation between news sentiment and price movements for a cryptocurrency")
    public String analyzeSentimentPriceCorrelation(String cryptocurrency) {
        try {
            log.info("Analyzing sentiment-price correlation for: {}", cryptocurrency);
            return analyticsService.analyzeSentimentPriceCorrelation(cryptocurrency);
        } catch (Exception e) {
            log.error("Error analyzing sentiment-price correlation", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
