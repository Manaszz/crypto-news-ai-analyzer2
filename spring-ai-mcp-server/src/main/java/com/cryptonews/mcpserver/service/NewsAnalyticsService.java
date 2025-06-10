package com.cryptonews.mcpserver.service;

import com.cryptonews.mcpserver.client.PerplexityNewsClient;
import com.cryptonews.mcpserver.model.CryptoAnalytics;
import com.cryptonews.mcpserver.model.NewsItem;
import com.cryptonews.mcpserver.model.NewsItemRepository;
import com.cryptonews.mcpserver.model.SentimentScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Основной сервис для аналитики криптовалютных новостей.
 */
@Service
@Transactional
@Slf4j
public class NewsAnalyticsService {

    private final PerplexityNewsClient perplexityClient;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final NewsItemRepository newsRepository;

    @Value("${news.analytics.max-articles-per-request:20}")
    private int maxArticlesPerRequest;

    @Value("${news.analytics.default-time-range-hours:24}")
    private int defaultTimeRangeHours;

    @Autowired
    public NewsAnalyticsService(PerplexityNewsClient perplexityClient,
                               SentimentAnalyzer sentimentAnalyzer,
                               NewsItemRepository newsRepository) {
        this.perplexityClient = perplexityClient;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.newsRepository = newsRepository;
    }

    /**
     * Получает последние новости по криптовалюте.
     */
    @Cacheable(value = "news-cache", key = "#cryptocurrency.toLowerCase() + '-' + #maxArticles")
    public List<NewsItem> getLatestCryptoNews(String cryptocurrency, Integer maxArticles) {
        log.info("Fetching latest news for cryptocurrency: {}", cryptocurrency);
        
        int articlesToFetch = maxArticles != null ? maxArticles : maxArticlesPerRequest;
        
        try {
            // Получаем новости из Perplexity
            List<String> newsTexts = perplexityClient.getCryptoNews(cryptocurrency, articlesToFetch);
            
            List<NewsItem> newsItems = new ArrayList<>();
            for (String newsText : newsTexts) {
                NewsItem newsItem = createNewsItem(newsText, cryptocurrency);
                newsItems.add(newsItem);
                
                // Сохраняем в базе данных
                try {
                    newsRepository.save(newsItem);
                } catch (Exception e) {
                    log.warn("Failed to save news item to database", e);
                }
            }
            
            log.info("Retrieved {} news items for {}", newsItems.size(), cryptocurrency);
            return newsItems;
            
        } catch (Exception e) {
            log.error("Error fetching latest crypto news for {}", cryptocurrency, e);
            // Fallback к сохраненным новостям
            return newsRepository.findTop10ByCryptocurrencyIgnoreCaseOrderByPublishedDateDesc(cryptocurrency);
        }
    }

    /**
     * Выполняет комплексный анализ криптовалюты.
     */
    @Cacheable(value = "analytics-cache", key = "#cryptocurrency.toLowerCase() + '-' + #timeRange")
    public CryptoAnalytics analyzeCryptocurrency(String cryptocurrency, String timeRange) {
        log.info("Performing comprehensive analysis for cryptocurrency: {} over {}", cryptocurrency, timeRange);
        
        try {
            // Получаем свежие новости
            List<NewsItem> newsItems = getLatestCryptoNews(cryptocurrency, maxArticlesPerRequest);
            
            // Получаем исторические данные из базы
            LocalDateTime startTime = calculateStartTime(timeRange);
            List<NewsItem> historicalNews = newsRepository.findByCryptocurrencyIgnoreCaseAndPublishedDateBetween(
                    cryptocurrency, startTime, LocalDateTime.now());
            
            // Объединяем новости
            Set<NewsItem> allNews = new HashSet<>(newsItems);
            allNews.addAll(historicalNews);
            List<NewsItem> combinedNews = new ArrayList<>(allNews);
            
            // Анализируем настроения
            Map<SentimentScore.SentimentLabel, List<NewsItem>> sentimentGroups = combinedNews.stream()
                    .collect(Collectors.groupingBy(news -> 
                            news.getSentimentScore() != null ? news.getSentimentScore().getLabel() : SentimentScore.SentimentLabel.NEUTRAL));
            
            // Вычисляем статистики
            int totalArticles = combinedNews.size();
            int positiveCount = sentimentGroups.getOrDefault(SentimentScore.SentimentLabel.POSITIVE, Collections.emptyList()).size();
            int negativeCount = sentimentGroups.getOrDefault(SentimentScore.SentimentLabel.NEGATIVE, Collections.emptyList()).size();
            int neutralCount = sentimentGroups.getOrDefault(SentimentScore.SentimentLabel.NEUTRAL, Collections.emptyList()).size();
            
            double averageSentiment = combinedNews.stream()
                    .filter(news -> news.getSentimentScore() != null)
                    .mapToDouble(news -> news.getSentimentScore().getCompoundScore())
                    .average()
                    .orElse(0.0);
            
            // Получаем дополнительную аналитику от Perplexity
            String marketSentimentAnalysis = perplexityClient.analyzeMarketSentiment(cryptocurrency, timeRange);
            String trendForecast = perplexityClient.getTrendForecast(cryptocurrency);
            String marketMovingEvents = perplexityClient.getMarketMovingEvents(cryptocurrency);
            
            // Извлекаем ключевые темы
            List<String> keyTopics = extractKeyTopics(combinedNews);
            
            // Получаем топ новости
            List<NewsItem> topPositive = sentimentGroups.getOrDefault(SentimentScore.SentimentLabel.POSITIVE, Collections.emptyList())
                    .stream()
                    .sorted((a, b) -> Double.compare(
                            b.getSentimentScore() != null ? b.getSentimentScore().getCompoundScore() : 0,
                            a.getSentimentScore() != null ? a.getSentimentScore().getCompoundScore() : 0))
                    .limit(3)
                    .collect(Collectors.toList());
            
            List<NewsItem> topNegative = sentimentGroups.getOrDefault(SentimentScore.SentimentLabel.NEGATIVE, Collections.emptyList())
                    .stream()
                    .sorted((a, b) -> Double.compare(
                            a.getSentimentScore() != null ? a.getSentimentScore().getCompoundScore() : 0,
                            b.getSentimentScore() != null ? b.getSentimentScore().getCompoundScore() : 0))
                    .limit(3)
                    .collect(Collectors.toList());
            
            // Вычисляем тренд настроения
            double sentimentTrend = calculateSentimentTrend(combinedNews);
            
            // Создаем результат
            return CryptoAnalytics.builder()
                    .cryptocurrency(cryptocurrency)
                    .timeRange(timeRange)
                    .totalArticles(totalArticles)
                    .sentimentCounts(positiveCount, negativeCount, neutralCount)
                    .averageSentiment(averageSentiment)
                    .sentimentTrend(sentimentTrend)
                    .keyTopics(keyTopics)
                    .topPositiveNews(topPositive)
                    .topNegativeNews(topNegative)
                    .marketMovingEvents(marketMovingEvents)
                    .trendForecast(trendForecast)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error performing cryptocurrency analysis for {}", cryptocurrency, e);
            // Возвращаем базовую аналитику
            return createBasicAnalytics(cryptocurrency, timeRange);
        }
    }

    /**
     * Получает настроение рынка за период.
     */
    public String getMarketSentiment(String cryptocurrency, String timeRange) {
        log.info("Getting market sentiment for {} over {}", cryptocurrency, timeRange);
        
        try {
            return perplexityClient.analyzeMarketSentiment(cryptocurrency, timeRange);
        } catch (Exception e) {
            log.error("Error getting market sentiment for {}", cryptocurrency, e);
            return "Unable to retrieve market sentiment analysis at this time.";
        }
    }

    /**
     * Сравнивает криптовалюты.
     */
    public String compareCryptocurrencies(List<String> cryptocurrencies) {
        log.info("Comparing cryptocurrencies: {}", cryptocurrencies);
        
        if (cryptocurrencies == null || cryptocurrencies.isEmpty()) {
            return "No cryptocurrencies provided for comparison.";
        }
        
        try {
            return perplexityClient.compareCryptocurrencies(cryptocurrencies);
        } catch (Exception e) {
            log.error("Error comparing cryptocurrencies: {}", cryptocurrencies, e);
            return "Unable to perform cryptocurrency comparison at this time.";
        }
    }

    /**
     * Получает позитивные новости.
     */
    public List<NewsItem> getPositiveNews(String cryptocurrency, Integer limit) {
        log.info("Getting positive news for {} (limit: {})", cryptocurrency, limit);
        
        int newsLimit = limit != null ? limit : 10;
        
        try {
            // Используем готовый метод из репозитория
            List<NewsItem> positiveNews = newsRepository.findPositiveNewsByCryptocurrency(cryptocurrency);
            
            // Если недостаточно новостей, получаем новые
            if (positiveNews.size() < newsLimit) {
                List<NewsItem> freshNews = getLatestCryptoNews(cryptocurrency, maxArticlesPerRequest);
                freshNews.stream()
                        .filter(news -> news.getSentimentScore() != null && 
                                news.getSentimentScore().getLabel() == SentimentScore.SentimentLabel.POSITIVE)
                        .forEach(positiveNews::add);
            }
            
            return positiveNews.stream().limit(newsLimit).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting positive news for {}", cryptocurrency, e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает негативные новости.
     */
    public List<NewsItem> getNegativeNews(String cryptocurrency, Integer limit) {
        log.info("Getting negative news for {} (limit: {})", cryptocurrency, limit);
        
        int newsLimit = limit != null ? limit : 10;
        
        try {
            // Используем готовый метод из репозитория
            List<NewsItem> negativeNews = newsRepository.findNegativeNewsByCryptocurrency(cryptocurrency);
            
            // Если недостаточно новостей, получаем новые
            if (negativeNews.size() < newsLimit) {
                List<NewsItem> freshNews = getLatestCryptoNews(cryptocurrency, maxArticlesPerRequest);
                freshNews.stream()
                        .filter(news -> news.getSentimentScore() != null && 
                                news.getSentimentScore().getLabel() == SentimentScore.SentimentLabel.NEGATIVE)
                        .forEach(negativeNews::add);
            }
            
            return negativeNews.stream().limit(newsLimit).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting negative news for {}", cryptocurrency, e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает прогноз трендов.
     */
    public String getTrendForecast(String cryptocurrency) {
        log.info("Getting trend forecast for {}", cryptocurrency);
        
        try {
            return perplexityClient.getTrendForecast(cryptocurrency);
        } catch (Exception e) {
            log.error("Error getting trend forecast for {}", cryptocurrency, e);
            return "Unable to retrieve trend forecast at this time.";
        }
    }

    /**
     * Ищет новости по ключевым словам.
     */
    public List<NewsItem> searchCryptoNews(String cryptocurrency, String keywords) {
        log.info("Searching crypto news for {} with keywords: {}", cryptocurrency, keywords);
        
        try {
            // Получаем свежие новости через поиск
            List<String> searchResults = perplexityClient.searchCryptoNews(cryptocurrency, keywords);
            
            List<NewsItem> newsItems = new ArrayList<>();
            for (String newsText : searchResults) {
                NewsItem newsItem = createNewsItem(newsText, cryptocurrency);
                newsItems.add(newsItem);
            }
            
            // Также ищем в существующих новостях
            List<NewsItem> matchingNews = newsRepository.searchByKeyword(cryptocurrency, keywords);
            
            // Объединяем результаты
            Set<NewsItem> allResults = new HashSet<>(newsItems);
            allResults.addAll(matchingNews);
            
            return new ArrayList<>(allResults);
            
        } catch (Exception e) {
            log.error("Error searching crypto news for {} with keywords: {}", cryptocurrency, keywords, e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает события, влияющие на рынок.
     */
    public String getMarketMovingEvents(String cryptocurrency) {
        log.info("Getting market moving events for {}", cryptocurrency);
        
        try {
            return perplexityClient.getMarketMovingEvents(cryptocurrency);
        } catch (Exception e) {
            log.error("Error getting market moving events for {}", cryptocurrency, e);
            return "Unable to retrieve market moving events at this time.";
        }
    }

    /**
     * Анализирует корреляцию настроения и цены.
     */
    public String analyzeSentimentPriceCorrelation(String cryptocurrency) {
        log.info("Analyzing sentiment-price correlation for {}", cryptocurrency);
        
        try {
            return perplexityClient.analyzeSentimentPriceCorrelation(cryptocurrency);
        } catch (Exception e) {
            log.error("Error analyzing sentiment-price correlation for {}", cryptocurrency, e);
            return "Unable to perform sentiment-price correlation analysis at this time.";
        }
    }

    // Приватные вспомогательные методы

    private NewsItem createNewsItem(String newsText, String cryptocurrency) {
        NewsItem newsItem = new NewsItem();
        newsItem.setCryptocurrency(cryptocurrency);
        newsItem.setPublishedDate(LocalDateTime.now());
        
        // Извлекаем заголовок (первая строка или до 100 символов)
        String title = newsText.length() > 100 ? 
                newsText.substring(0, 100) + "..." : newsText;
        newsItem.setTitle(title);
        newsItem.setDescription(newsText);
        newsItem.setSource("Perplexity AI");
        
        // Анализируем настроение
        SentimentScore sentiment = sentimentAnalyzer.analyzeSentiment(newsText);
        newsItem.setSentimentScore(sentiment);
        
        // Генерируем URL (поскольку это могут быть сводные данные)
        newsItem.setUrl("https://perplexity.ai/search?q=" + 
                cryptocurrency.replace(" ", "+") + "+news");
        
        return newsItem;
    }

    private LocalDateTime calculateStartTime(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        
        if (timeRange == null || timeRange.isEmpty()) {
            return now.minusHours(defaultTimeRangeHours);
        }
        
        String lowerRange = timeRange.toLowerCase();
        
        if (lowerRange.contains("hour")) {
            int hours = extractNumber(lowerRange, defaultTimeRangeHours);
            return now.minusHours(hours);
        } else if (lowerRange.contains("day")) {
            int days = extractNumber(lowerRange, 1);
            return now.minusDays(days);
        } else if (lowerRange.contains("week")) {
            int weeks = extractNumber(lowerRange, 1);
            return now.minusWeeks(weeks);
        } else if (lowerRange.contains("month")) {
            int months = extractNumber(lowerRange, 1);
            return now.minusMonths(months);
        }
        
        return now.minusHours(defaultTimeRangeHours);
    }

    private int extractNumber(String text, int defaultValue) {
        try {
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.matches("\\d+")) {
                    return Integer.parseInt(word);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Could not extract number from: {}", text);
        }
        return defaultValue;
    }

    private List<String> extractKeyTopics(List<NewsItem> newsItems) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (NewsItem news : newsItems) {
            String content = (news.getTitle() + " " + (news.getDescription() != null ? news.getDescription() : "")).toLowerCase();
            String[] words = content.split("\\W+");
            
            for (String word : words) {
                if (word.length() > 3 && !isStopWord(word)) {
                    wordFrequency.merge(word, 1, Integer::sum);
                }
            }
        }
        
        return wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("this", "that", "with", "have", "will", "from", "they", "been", 
                "their", "said", "each", "which", "their", "would", "there", "what", "about", "said");
        return stopWords.contains(word);
    }

    private double calculateSentimentTrend(List<NewsItem> newsItems) {
        if (newsItems.size() < 2) {
            return 0.0;
        }
        
        // Сортируем по времени
        List<NewsItem> sorted = newsItems.stream()
                .filter(news -> news.getSentimentScore() != null)
                .sorted(Comparator.comparing(NewsItem::getPublishedDate))
                .collect(Collectors.toList());
        
        if (sorted.size() < 2) {
            return 0.0;
        }
        
        // Берем первую и последнюю половину для сравнения
        int midPoint = sorted.size() / 2;
        
        double firstHalfAvg = sorted.subList(0, midPoint).stream()
                .mapToDouble(news -> news.getSentimentScore().getCompoundScore())
                .average()
                .orElse(0.0);
        
        double secondHalfAvg = sorted.subList(midPoint, sorted.size()).stream()
                .mapToDouble(news -> news.getSentimentScore().getCompoundScore())
                .average()
                .orElse(0.0);
        
        return secondHalfAvg - firstHalfAvg;
    }

    private boolean containsKeywords(NewsItem news, String keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        
        String content = (news.getTitle() + " " + (news.getDescription() != null ? news.getDescription() : "")).toLowerCase();
        String[] keywordArray = keywords.toLowerCase().split("\\s+");
        
        for (String keyword : keywordArray) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }

    private CryptoAnalytics createBasicAnalytics(String cryptocurrency, String timeRange) {
        return CryptoAnalytics.builder()
                .cryptocurrency(cryptocurrency)
                .timeRange(timeRange)
                .totalArticles(0)
                .sentimentCounts(0, 0, 0)
                .averageSentiment(0.0)
                .sentimentTrend(0.0)
                .keyTopics(Collections.emptyList())
                .topPositiveNews(Collections.emptyList())
                .topNegativeNews(Collections.emptyList())
                .marketMovingEvents("No data available")
                .trendForecast("No forecast available")
                .build();
    }
} 
