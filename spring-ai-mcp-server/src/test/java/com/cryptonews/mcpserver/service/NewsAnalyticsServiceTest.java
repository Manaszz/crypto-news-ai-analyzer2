package com.cryptonews.mcpserver.service;

import com.cryptonews.mcpserver.client.PerplexityNewsClient;
import com.cryptonews.mcpserver.model.NewsItem;
import com.cryptonews.mcpserver.model.NewsItemRepository;
import com.cryptonews.mcpserver.model.SentimentScore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsAnalyticsServiceTest {

    @Mock
    private PerplexityNewsClient perplexityNewsClient;

    @Mock
    private NewsItemRepository newsRepository;

    @Mock
    private SentimentAnalyzer sentimentAnalyzer;

    @InjectMocks
    private NewsAnalyticsService newsAnalyticsService;

    @Test
    public void testGetLatestCryptoNews() {
        // Given
        String cryptocurrency = "BTC";
        List<String> mockNewsTexts = Arrays.asList(
                "Bitcoin reaches new highs", 
                "BTC shows positive momentum"
        );
        
        NewsItem mockNewsItem = createMockNewsItem("Bitcoin reaches new highs", cryptocurrency);
        
        when(perplexityNewsClient.getCryptoNews(anyString(), anyInt())).thenReturn(mockNewsTexts);
        when(sentimentAnalyzer.analyzeSentiment(anyString())).thenReturn(new SentimentScore(0.7, 0.1, 0.2, 0.6));
        when(newsRepository.save(any(NewsItem.class))).thenReturn(mockNewsItem);

        // When
        List<NewsItem> result = newsAnalyticsService.getLatestCryptoNews(cryptocurrency, 5);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void testGetMarketSentiment() {
        // Given
        String cryptocurrency = "BTC";
        String timeRange = "24h";
        String expectedSentiment = "Market shows positive sentiment for Bitcoin";
        
        when(perplexityNewsClient.analyzeMarketSentiment(cryptocurrency, timeRange))
                .thenReturn(expectedSentiment);

        // When
        String result = newsAnalyticsService.getMarketSentiment(cryptocurrency, timeRange);

        // Then
        assertThat(result).isEqualTo(expectedSentiment);
    }

    @Test
    public void testGetPositiveNews() {
        // Given
        String cryptocurrency = "ETH";
        NewsItem positiveNews = createMockNewsItem("Ethereum shows great progress", cryptocurrency);
        positiveNews.setSentimentScore(new SentimentScore(0.8, 0.1, 0.1, 0.7));
        
        when(newsRepository.findPositiveNewsByCryptocurrency(cryptocurrency))
                .thenReturn(Arrays.asList(positiveNews));

        // When
        List<NewsItem> result = newsAnalyticsService.getPositiveNews(cryptocurrency, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Ethereum");
    }

    @Test
    public void testGetNegativeNews() {
        // Given
        String cryptocurrency = "ETH";
        NewsItem negativeNews = createMockNewsItem("Ethereum faces challenges", cryptocurrency);
        negativeNews.setSentimentScore(new SentimentScore(0.1, 0.8, 0.1, -0.7));
        
        when(newsRepository.findNegativeNewsByCryptocurrency(cryptocurrency))
                .thenReturn(Arrays.asList(negativeNews));

        // When
        List<NewsItem> result = newsAnalyticsService.getNegativeNews(cryptocurrency, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("challenges");
    }

    private NewsItem createMockNewsItem(String title, String cryptocurrency) {
        NewsItem newsItem = new NewsItem();
        newsItem.setTitle(title);
        newsItem.setDescription(title + " - detailed description");
        newsItem.setCryptocurrency(cryptocurrency);
        newsItem.setPublishedDate(LocalDateTime.now());
        newsItem.setSource("Test Source");
        newsItem.setUrl("https://test.com/news");
        return newsItem;
    }
} 