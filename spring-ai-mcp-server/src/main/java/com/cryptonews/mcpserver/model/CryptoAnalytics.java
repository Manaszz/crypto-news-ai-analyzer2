package com.cryptonews.mcpserver.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class CryptoAnalytics {

    String cryptocurrency;
    String timeRange;
    int totalArticles;
    Map<String, Integer> sentimentCounts;
    double averageSentiment;
    double sentimentTrend;
    List<String> keyTopics;
    List<NewsItem> topPositiveNews;
    List<NewsItem> topNegativeNews;
    String marketMovingEvents;
    String trendForecast;

    public static class CryptoAnalyticsBuilder {
        public CryptoAnalyticsBuilder sentimentCounts(int positive, int negative, int neutral) {
            this.sentimentCounts = Map.of("positive", positive, "negative", negative, "neutral", neutral);
            return this;
        }
    }
} 