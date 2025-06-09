package com.cryptonews.mcpserver.tools;

import com.cryptonews.mcpserver.model.CryptoAnalytics;
import com.cryptonews.mcpserver.model.NewsItem;
import com.cryptonews.mcpserver.service.NewsAnalyticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * MCP инструменты для анализа криптовалютных новостей.
 * Предоставляет набор функций для Claude Desktop и других MCP клиентов.
 */
@Component
public class CryptoNewsTools {

    private static final Logger logger = LoggerFactory.getLogger(CryptoNewsTools.class);

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
    @Bean
    @Description("Get the latest cryptocurrency news for a specific coin")
    public FunctionCallback getLatestCryptoNews() {
        return FunctionCallbackWrapper.builder(new GetLatestNewsFunction())
                .withName("getLatestCryptoNews")
                .withDescription("Retrieves the latest news articles for a specified cryptocurrency with sentiment analysis")
                .build();
    }

    /**
     * Выполняет комплексный анализ криптовалюты.
     */
    @Bean
    @Description("Perform comprehensive cryptocurrency analysis including sentiment trends and market insights")
    public FunctionCallback analyzeCryptocurrency() {
        return FunctionCallbackWrapper.builder(new AnalyzeCryptocurrencyFunction())
                .withName("analyzeCryptocurrency")
                .withDescription("Performs comprehensive analysis of cryptocurrency including sentiment analysis, trends, and market insights over a specified time period")
                .build();
    }

    /**
     * Получает настроение рынка за период.
     */
    @Bean
    @Description("Get market sentiment analysis for a cryptocurrency over a time period")
    public FunctionCallback getMarketSentiment() {
        return FunctionCallbackWrapper.builder(new GetMarketSentimentFunction())
                .withName("getMarketSentiment")
                .withDescription("Analyzes overall market sentiment for a cryptocurrency over a specified time range")
                .build();
    }

    /**
     * Сравнивает криптовалюты.
     */
    @Bean
    @Description("Compare multiple cryptocurrencies based on recent news and market sentiment")
    public FunctionCallback compareCryptocurrencies() {
        return FunctionCallbackWrapper.builder(new CompareCryptocurrenciesFunction())
                .withName("compareCryptocurrencies")
                .withDescription("Compares multiple cryptocurrencies based on recent market performance, news sentiment, and key developments")
                .build();
    }

    /**
     * Получает позитивные новости.
     */
    @Bean
    @Description("Get positive news for a cryptocurrency")
    public FunctionCallback getPositiveNews() {
        return FunctionCallbackWrapper.builder(new GetPositiveNewsFunction())
                .withName("getPositiveNews")
                .withDescription("Retrieves news articles with positive sentiment for a specified cryptocurrency")
                .build();
    }

    /**
     * Получает негативные новости.
     */
    @Bean
    @Description("Get negative news for a cryptocurrency")
    public FunctionCallback getNegativeNews() {
        return FunctionCallbackWrapper.builder(new GetNegativeNewsFunction())
                .withName("getNegativeNews")
                .withDescription("Retrieves news articles with negative sentiment for a specified cryptocurrency")
                .build();
    }

    /**
     * Получает прогноз трендов.
     */
    @Bean
    @Description("Get trend forecast for a cryptocurrency")
    public FunctionCallback getTrendForecast() {
        return FunctionCallbackWrapper.builder(new GetTrendForecastFunction())
                .withName("getTrendForecast")
                .withDescription("Provides short-term trend forecast for a cryptocurrency based on recent news and market analysis")
                .build();
    }

    /**
     * Ищет новости по ключевым словам.
     */
    @Bean
    @Description("Search cryptocurrency news by keywords")
    public FunctionCallback searchCryptoNews() {
        return FunctionCallbackWrapper.builder(new SearchCryptoNewsFunction())
                .withName("searchCryptoNews")
                .withDescription("Searches for cryptocurrency news articles containing specific keywords or topics")
                .build();
    }

    /**
     * Получает события, влияющие на рынок.
     */
    @Bean
    @Description("Get market-moving events for a cryptocurrency")
    public FunctionCallback getMarketMovingEvents() {
        return FunctionCallbackWrapper.builder(new GetMarketMovingEventsFunction())
                .withName("getMarketMovingEvents")
                .withDescription("Identifies recent significant events and news that have impacted or are likely to impact the cryptocurrency market")
                .build();
    }

    /**
     * Анализирует корреляцию настроения и цены.
     */
    @Bean
    @Description("Analyze sentiment-price correlation for a cryptocurrency")
    public FunctionCallback analyzeSentimentPriceCorrelation() {
        return FunctionCallbackWrapper.builder(new AnalyzeSentimentPriceCorrelationFunction())
                .withName("analyzeSentimentPriceCorrelation")
                .withDescription("Analyzes the correlation between news sentiment and price movements for a cryptocurrency")
                .build();
    }

    // Внутренние классы для функций

    /**
     * Запрос для получения последних новостей.
     */
    public static class GetLatestNewsRequest {
        public String cryptocurrency;
        public Integer maxArticles = 10;

        public GetLatestNewsRequest() {}

        public GetLatestNewsRequest(String cryptocurrency, Integer maxArticles) {
            this.cryptocurrency = cryptocurrency;
            this.maxArticles = maxArticles;
        }
    }

    /**
     * Функция получения последних новостей.
     */
    public class GetLatestNewsFunction implements Function<GetLatestNewsRequest, String> {
        @Override
        public String apply(GetLatestNewsRequest request) {
            try {
                logger.info("Getting latest news for: {} (max: {})", request.cryptocurrency, request.maxArticles);
                List<NewsItem> news = analyticsService.getLatestCryptoNews(request.cryptocurrency, request.maxArticles);
                return objectMapper.writeValueAsString(news);
            } catch (JsonProcessingException e) {
                logger.error("Error serializing latest news response", e);
                return "{\"error\": \"Failed to retrieve latest news\"}";
            } catch (Exception e) {
                logger.error("Error getting latest news", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для анализа криптовалюты.
     */
    public static class AnalyzeCryptocurrencyRequest {
        public String cryptocurrency;
        public String timeRange = "24 hours";

        public AnalyzeCryptocurrencyRequest() {}

        public AnalyzeCryptocurrencyRequest(String cryptocurrency, String timeRange) {
            this.cryptocurrency = cryptocurrency;
            this.timeRange = timeRange;
        }
    }

    /**
     * Функция анализа криптовалюты.
     */
    public class AnalyzeCryptocurrencyFunction implements Function<AnalyzeCryptocurrencyRequest, String> {
        @Override
        public String apply(AnalyzeCryptocurrencyRequest request) {
            try {
                logger.info("Analyzing cryptocurrency: {} over {}", request.cryptocurrency, request.timeRange);
                CryptoAnalytics analytics = analyticsService.analyzeCryptocurrency(request.cryptocurrency, request.timeRange);
                return objectMapper.writeValueAsString(analytics);
            } catch (JsonProcessingException e) {
                logger.error("Error serializing cryptocurrency analysis response", e);
                return "{\"error\": \"Failed to analyze cryptocurrency\"}";
            } catch (Exception e) {
                logger.error("Error analyzing cryptocurrency", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для получения настроения рынка.
     */
    public static class GetMarketSentimentRequest {
        public String cryptocurrency;
        public String timeRange = "24 hours";

        public GetMarketSentimentRequest() {}

        public GetMarketSentimentRequest(String cryptocurrency, String timeRange) {
            this.cryptocurrency = cryptocurrency;
            this.timeRange = timeRange;
        }
    }

    /**
     * Функция получения настроения рынка.
     */
    public class GetMarketSentimentFunction implements Function<GetMarketSentimentRequest, String> {
        @Override
        public String apply(GetMarketSentimentRequest request) {
            try {
                logger.info("Getting market sentiment for: {} over {}", request.cryptocurrency, request.timeRange);
                String sentiment = analyticsService.getMarketSentiment(request.cryptocurrency, request.timeRange);
                return "{\"sentiment\": \"" + sentiment.replace("\"", "\\\"") + "\"}";
            } catch (Exception e) {
                logger.error("Error getting market sentiment", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для сравнения криптовалют.
     */
    public static class CompareCryptocurrenciesRequest {
        public List<String> cryptocurrencies;

        public CompareCryptocurrenciesRequest() {}

        public CompareCryptocurrenciesRequest(List<String> cryptocurrencies) {
            this.cryptocurrencies = cryptocurrencies;
        }
    }

    /**
     * Функция сравнения криптовалют.
     */
    public class CompareCryptocurrenciesFunction implements Function<CompareCryptocurrenciesRequest, String> {
        @Override
        public String apply(CompareCryptocurrenciesRequest request) {
            try {
                logger.info("Comparing cryptocurrencies: {}", request.cryptocurrencies);
                String comparison = analyticsService.compareCryptocurrencies(request.cryptocurrencies);
                return "{\"comparison\": \"" + comparison.replace("\"", "\\\"") + "\"}";
            } catch (Exception e) {
                logger.error("Error comparing cryptocurrencies", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для получения позитивных новостей.
     */
    public static class GetPositiveNewsRequest {
        public String cryptocurrency;
        public Integer limit = 10;

        public GetPositiveNewsRequest() {}

        public GetPositiveNewsRequest(String cryptocurrency, Integer limit) {
            this.cryptocurrency = cryptocurrency;
            this.limit = limit;
        }
    }

    /**
     * Функция получения позитивных новостей.
     */
    public class GetPositiveNewsFunction implements Function<GetPositiveNewsRequest, String> {
        @Override
        public String apply(GetPositiveNewsRequest request) {
            try {
                logger.info("Getting positive news for: {} (limit: {})", request.cryptocurrency, request.limit);
                List<NewsItem> news = analyticsService.getPositiveNews(request.cryptocurrency, request.limit);
                return objectMapper.writeValueAsString(news);
            } catch (JsonProcessingException e) {
                logger.error("Error serializing positive news response", e);
                return "{\"error\": \"Failed to retrieve positive news\"}";
            } catch (Exception e) {
                logger.error("Error getting positive news", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для получения негативных новостей.
     */
    public static class GetNegativeNewsRequest {
        public String cryptocurrency;
        public Integer limit = 10;

        public GetNegativeNewsRequest() {}

        public GetNegativeNewsRequest(String cryptocurrency, Integer limit) {
            this.cryptocurrency = cryptocurrency;
            this.limit = limit;
        }
    }

    /**
     * Функция получения негативных новостей.
     */
    public class GetNegativeNewsFunction implements Function<GetNegativeNewsRequest, String> {
        @Override
        public String apply(GetNegativeNewsRequest request) {
            try {
                logger.info("Getting negative news for: {} (limit: {})", request.cryptocurrency, request.limit);
                List<NewsItem> news = analyticsService.getNegativeNews(request.cryptocurrency, request.limit);
                return objectMapper.writeValueAsString(news);
            } catch (JsonProcessingException e) {
                logger.error("Error serializing negative news response", e);
                return "{\"error\": \"Failed to retrieve negative news\"}";
            } catch (Exception e) {
                logger.error("Error getting negative news", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для получения прогноза трендов.
     */
    public static class GetTrendForecastRequest {
        public String cryptocurrency;

        public GetTrendForecastRequest() {}

        public GetTrendForecastRequest(String cryptocurrency) {
            this.cryptocurrency = cryptocurrency;
        }
    }

    /**
     * Функция получения прогноза трендов.
     */
    public class GetTrendForecastFunction implements Function<GetTrendForecastRequest, String> {
        @Override
        public String apply(GetTrendForecastRequest request) {
            try {
                logger.info("Getting trend forecast for: {}", request.cryptocurrency);
                String forecast = analyticsService.getTrendForecast(request.cryptocurrency);
                return "{\"forecast\": \"" + forecast.replace("\"", "\\\"") + "\"}";
            } catch (Exception e) {
                logger.error("Error getting trend forecast", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для поиска новостей.
     */
    public static class SearchCryptoNewsRequest {
        public String cryptocurrency;
        public String keywords;

        public SearchCryptoNewsRequest() {}

        public SearchCryptoNewsRequest(String cryptocurrency, String keywords) {
            this.cryptocurrency = cryptocurrency;
            this.keywords = keywords;
        }
    }

    /**
     * Функция поиска новостей.
     */
    public class SearchCryptoNewsFunction implements Function<SearchCryptoNewsRequest, String> {
        @Override
        public String apply(SearchCryptoNewsRequest request) {
            try {
                logger.info("Searching crypto news for: {} with keywords: {}", request.cryptocurrency, request.keywords);
                List<NewsItem> news = analyticsService.searchCryptoNews(request.cryptocurrency, request.keywords);
                return objectMapper.writeValueAsString(news);
            } catch (JsonProcessingException e) {
                logger.error("Error serializing search results", e);
                return "{\"error\": \"Failed to search crypto news\"}";
            } catch (Exception e) {
                logger.error("Error searching crypto news", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для получения событий, влияющих на рынок.
     */
    public static class GetMarketMovingEventsRequest {
        public String cryptocurrency;

        public GetMarketMovingEventsRequest() {}

        public GetMarketMovingEventsRequest(String cryptocurrency) {
            this.cryptocurrency = cryptocurrency;
        }
    }

    /**
     * Функция получения событий, влияющих на рынок.
     */
    public class GetMarketMovingEventsFunction implements Function<GetMarketMovingEventsRequest, String> {
        @Override
        public String apply(GetMarketMovingEventsRequest request) {
            try {
                logger.info("Getting market moving events for: {}", request.cryptocurrency);
                String events = analyticsService.getMarketMovingEvents(request.cryptocurrency);
                return "{\"events\": \"" + events.replace("\"", "\\\"") + "\"}";
            } catch (Exception e) {
                logger.error("Error getting market moving events", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    /**
     * Запрос для анализа корреляции настроения и цены.
     */
    public static class AnalyzeSentimentPriceCorrelationRequest {
        public String cryptocurrency;

        public AnalyzeSentimentPriceCorrelationRequest() {}

        public AnalyzeSentimentPriceCorrelationRequest(String cryptocurrency) {
            this.cryptocurrency = cryptocurrency;
        }
    }

    /**
     * Функция анализа корреляции настроения и цены.
     */
    public class AnalyzeSentimentPriceCorrelationFunction implements Function<AnalyzeSentimentPriceCorrelationRequest, String> {
        @Override
        public String apply(AnalyzeSentimentPriceCorrelationRequest request) {
            try {
                logger.info("Analyzing sentiment-price correlation for: {}", request.cryptocurrency);
                String correlation = analyticsService.analyzeSentimentPriceCorrelation(request.cryptocurrency);
                return "{\"correlation\": \"" + correlation.replace("\"", "\\\"") + "\"}";
            } catch (Exception e) {
                logger.error("Error analyzing sentiment-price correlation", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
    }
}