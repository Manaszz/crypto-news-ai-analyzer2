package com.cryptonews.mcpserver.controller;

import com.cryptonews.mcpserver.tools.CryptoNewsTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Тестовый контроллер для проверки работы MCP инструментов
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    private final CryptoNewsTools cryptoNewsTools;

    @Autowired
    public TestController(CryptoNewsTools cryptoNewsTools) {
        this.cryptoNewsTools = cryptoNewsTools;
    }

    @GetMapping("/health")
    public String health() {
        return "{\"status\": \"UP\", \"message\": \"MCP Tools are ready\"}";
    }

    @PostMapping("/getLatestNews")
    public String getLatestNews(@RequestParam String cryptocurrency, 
                               @RequestParam(defaultValue = "5") Integer maxArticles) {
        log.info("Test call to getLatestCryptoNews: {} ({})", cryptocurrency, maxArticles);
        return cryptoNewsTools.getLatestCryptoNews(cryptocurrency, maxArticles);
    }

    @PostMapping("/analyzeCrypto")
    public String analyzeCrypto(@RequestParam String cryptocurrency,
                               @RequestParam(defaultValue = "24 hours") String timeRange) {
        log.info("Test call to analyzeCryptocurrency: {} ({})", cryptocurrency, timeRange);
        return cryptoNewsTools.analyzeCryptocurrency(cryptocurrency, timeRange);
    }

    @PostMapping("/getMarketSentiment")
    public String getMarketSentiment(@RequestParam String cryptocurrency,
                                   @RequestParam(defaultValue = "24 hours") String timeRange) {
        log.info("Test call to getMarketSentiment: {} ({})", cryptocurrency, timeRange);
        return cryptoNewsTools.getMarketSentiment(cryptocurrency, timeRange);
    }

    @GetMapping("/listTools")
    public String listTools() {
        return """
            {
              "tools": [
                {"name": "getLatestCryptoNews", "description": "Get latest news for cryptocurrency"},
                {"name": "analyzeCryptocurrency", "description": "Comprehensive crypto analysis"},
                {"name": "getMarketSentiment", "description": "Market sentiment analysis"},
                {"name": "compareCryptocurrencies", "description": "Compare multiple cryptocurrencies"},
                {"name": "getPositiveNews", "description": "Get positive news"},
                {"name": "getNegativeNews", "description": "Get negative news"},
                {"name": "getTrendForecast", "description": "Trend forecast"},
                {"name": "searchCryptoNews", "description": "Search news by keywords"},
                {"name": "getMarketMovingEvents", "description": "Market moving events"},
                {"name": "analyzeSentimentPriceCorrelation", "description": "Sentiment-price correlation"}
              ]
            }
            """;
    }
} 