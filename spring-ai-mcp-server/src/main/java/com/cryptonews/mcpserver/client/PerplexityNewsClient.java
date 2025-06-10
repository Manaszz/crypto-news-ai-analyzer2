package com.cryptonews.mcpserver.client;

import com.cryptonews.mcpserver.client.dto.PerplexitySearchResponse;
import com.cryptonews.mcpserver.model.NewsResponse;
import com.cryptonews.mcpserver.model.PerplexityRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PerplexityNewsClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public PerplexityNewsClient(WebClient webClient) {
        this.webClient = webClient;
        this.apiKey = "demo-key";
        this.model = "sonar-small-chat";
    }

    @Autowired
    public PerplexityNewsClient(WebClient.Builder webClientBuilder,
                                @Value("${perplexity.api.url}") String perplexityApiUrl,
                                @Value("${perplexity.api.key}") String apiKey,
                                @Value("${perplexity.api.model}") String model) {
        this.webClient = webClientBuilder.baseUrl(perplexityApiUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public Mono<NewsResponse> searchNews(String query) {
        log.info("Searching news from Perplexity with query: {}", query);
        PerplexityRequest request = new PerplexityRequest(this.model,
                List.of(new PerplexityRequest.Message("user", query)));

        return this.webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + this.apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue(request))
                .retrieve()
                .bodyToMono(PerplexitySearchResponse.class)
                .map(response -> {
                    String content = response.choices().get(0).message().content();
                    String[] parts = content.split("\n");
                    return new NewsResponse(parts.length > 0 ? parts[0] : "No title", 
                                          parts.length > 1 ? parts[1] : "Perplexity", 
                                          parts.length > 2 ? parts[2] : "https://perplexity.ai");
                })
                .doOnError(e -> log.error("Error calling Perplexity API", e))
                .retry(3);
    }

    /**
     * Получает криптовалютные новости с анализом.
     */
    public List<String> getCryptoNews(String cryptocurrency, int maxArticles) {
        log.info("Getting crypto news for {} (max: {})", cryptocurrency, maxArticles);
        
        String query = String.format("Get the latest %d news articles about %s cryptocurrency. " +
                "Provide a brief summary of each article focusing on market impact and sentiment.", 
                maxArticles, cryptocurrency);
        
        try {
            NewsResponse response = searchNews(query).block();
            if (response != null && response.title() != null) {
                // Разбиваем ответ на отдельные новости
                String content = response.title() + "\n" + response.source() + "\n" + response.url();
                return Arrays.asList(content.split("\\. "));
            }
            return List.of(String.format("Unable to fetch news for %s at this time.", cryptocurrency));
        } catch (Exception e) {
            log.error("Error getting crypto news for {}", cryptocurrency, e);
            return List.of(String.format("Error fetching news for %s: %s", cryptocurrency, e.getMessage()));
        }
    }

    /**
     * Анализирует настроение рынка.
     */
    public String analyzeMarketSentiment(String cryptocurrency, String timeRange) {
        log.info("Analyzing market sentiment for {} over {}", cryptocurrency, timeRange);
        
        String query = String.format("Analyze the current market sentiment for %s cryptocurrency over the past %s. " +
                "Consider recent news, social media buzz, and market movements. " +
                "Provide a comprehensive sentiment analysis with bullish/bearish indicators.", 
                cryptocurrency, timeRange);
        
        try {
            NewsResponse response = searchNews(query).block();
            return response != null ? response.title() : 
                "Unable to analyze market sentiment at this time.";
        } catch (Exception e) {
            log.error("Error analyzing market sentiment for {}", cryptocurrency, e);
            return String.format("Error analyzing sentiment for %s: %s", cryptocurrency, e.getMessage());
        }
    }

    /**
     * Получает прогноз трендов.
     */
    public String getTrendForecast(String cryptocurrency) {
        log.info("Getting trend forecast for {}", cryptocurrency);
        
        String query = String.format("Provide a short-term trend forecast for %s cryptocurrency. " +
                "Consider technical analysis, recent developments, market conditions, " +
                "and institutional adoption. Give concrete price predictions if possible.", 
                cryptocurrency);
        
        try {
            NewsResponse response = searchNews(query).block();
            return response != null ? response.title() : 
                "Unable to generate trend forecast at this time.";
        } catch (Exception e) {
            log.error("Error getting trend forecast for {}", cryptocurrency, e);
            return String.format("Error getting forecast for %s: %s", cryptocurrency, e.getMessage());
        }
    }

    /**
     * Получает события, влияющие на рынок.
     */
    public String getMarketMovingEvents(String cryptocurrency) {
        log.info("Getting market moving events for {}", cryptocurrency);
        
        String query = String.format("Identify the most significant recent events affecting %s cryptocurrency. " +
                "Include regulatory news, partnerships, technical upgrades, market movements, " +
                "and any major announcements that could impact price.", 
                cryptocurrency);
        
        try {
            NewsResponse response = searchNews(query).block();
            return response != null ? response.title() : 
                "No significant market-moving events identified at this time.";
        } catch (Exception e) {
            log.error("Error getting market moving events for {}", cryptocurrency, e);
            return String.format("Error getting events for %s: %s", cryptocurrency, e.getMessage());
        }
    }

    /**
     * Анализирует корреляцию настроения и цены.
     */
    public String analyzeSentimentPriceCorrelation(String cryptocurrency) {
        log.info("Analyzing sentiment-price correlation for {}", cryptocurrency);
        
        String query = String.format("Analyze the correlation between news sentiment and price movements for %s. " +
                "Examine how positive and negative news has historically affected the price. " +
                "Provide insights on sentiment-driven price movements and their reliability.", 
                cryptocurrency);
        
        try {
            NewsResponse response = searchNews(query).block();
            return response != null ? response.title() : 
                "Unable to analyze sentiment-price correlation at this time.";
        } catch (Exception e) {
            log.error("Error analyzing sentiment-price correlation for {}", cryptocurrency, e);
            return String.format("Error analyzing correlation for %s: %s", cryptocurrency, e.getMessage());
        }
    }

    /**
     * Сравнивает криптовалюты.
     */
    public String compareCryptocurrencies(List<String> cryptocurrencies) {
        log.info("Comparing cryptocurrencies: {}", cryptocurrencies);
        
        String cryptoList = String.join(", ", cryptocurrencies);
        String query = String.format("Compare the following cryptocurrencies: %s. " +
                "Analyze their recent performance, market sentiment, adoption rates, " +
                "technical developments, and investment potential. " +
                "Provide a detailed comparison with pros and cons for each.", 
                cryptoList);
        
        try {
            NewsResponse response = searchNews(query).block();
            return response != null ? response.title() : 
                "Unable to compare cryptocurrencies at this time.";
        } catch (Exception e) {
            log.error("Error comparing cryptocurrencies: {}", cryptocurrencies, e);
            return String.format("Error comparing cryptocurrencies: %s", e.getMessage());
        }
    }

    /**
     * Ищет новости по ключевым словам.
     */
    public List<String> searchCryptoNews(String cryptocurrency, String keywords) {
        log.info("Searching crypto news for {} with keywords: {}", cryptocurrency, keywords);
        
        String query = String.format("Search for recent news about %s cryptocurrency containing keywords: %s. " +
                "Provide relevant articles that match these topics and explain their significance.", 
                cryptocurrency, keywords);
        
        try {
            NewsResponse response = searchNews(query).block();
            if (response != null && response.title() != null) {
                String content = response.title() + "\n" + response.source() + "\n" + response.url();
                return Arrays.asList(content.split("\\. "));
            }
            return List.of(String.format("No news found for %s with keywords: %s", cryptocurrency, keywords));
        } catch (Exception e) {
            log.error("Error searching crypto news for {} with keywords: {}", cryptocurrency, keywords, e);
            return List.of(String.format("Error searching news: %s", e.getMessage()));
        }
    }
} 
