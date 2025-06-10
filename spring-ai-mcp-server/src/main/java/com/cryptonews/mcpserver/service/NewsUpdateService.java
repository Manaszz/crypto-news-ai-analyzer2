package com.cryptonews.mcpserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class NewsUpdateService {

    private final NewsAnalyticsService newsAnalyticsService;
    private final SseService sseService;
    private final List<String> trackedTokens = List.of("BTC", "ETH"); // Example tokens

    public NewsUpdateService(NewsAnalyticsService newsAnalyticsService, SseService sseService) {
        this.newsAnalyticsService = newsAnalyticsService;
        this.sseService = sseService;
    }

    @Scheduled(fixedRate = 60000) // Fetch news every 60 seconds
    public void fetchNewsUpdates() {
        log.info("Fetching news updates for tracked tokens...");
        trackedTokens.forEach(token -> {
            try {
                var newsItems = newsAnalyticsService.getLatestCryptoNews(token, 5);
                if (!newsItems.isEmpty()) {
                    newsItems.forEach(newsItem -> {
                        log.info("Sending news update for token {}: {}", token, newsItem.getTitle());
                        sseService.send("news-update", newsItem);
                    });
                }
            } catch (Exception e) {
                log.error("Error fetching news updates for token {}", token, e);
            }
        });
    }
} 
