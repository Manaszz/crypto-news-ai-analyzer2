package com.cryptonews.mcpserver.service;

import com.cryptonews.mcpserver.client.PerplexityNewsClient;
import com.cryptonews.mcpserver.model.CryptoAnalytics;
import com.cryptonews.mcpserver.model.NewsItem;
import com.cryptonews.mcpserver.model.NewsItemRepository;
import com.cryptonews.mcpserver.model.SentimentScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
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
public class NewsAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsAnalyticsService.class);

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
        logger.info("Fetching latest news for cryptocurrency: {}", cryptocurrency);
        
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
                    logger.warn("Failed to save news item to database", e);
                }
            }
            
            logger.info("Retrieved {} news items for {}", newsItems.size(), cryptocurrency);
            return newsItems;
            
        } catch (Exception e) {
            logger.error("Error fetching latest crypto news for {}", cryptocurrency, e);
            // Fallback к сохраненным новостям
            return newsRepository.findTop10ByCryptocurrencyIgnoreCaseOrderByPublishedDateDesc(cryptocurrency);
        }
    }

    /**
     * Выполняет комплексный анализ криптовалюты.
     */
    @Cacheable(value = "analytics-cache", key = "#cryptocurrency.toLowerCase() + '-' + #timeRange")
    public CryptoAnalytics analyzeCryptocurrency(String cryptocurrency, String timeRange) {
        logger.info("Performing comprehensive analysis for cryptocurrency: {} over {}", cryptocurrency, timeRange);
        
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
            return new CryptoAnalytics.Builder(cryptocurrency, timeRange)
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
            logger.error("Error performing cryptocurrency analysis for {}", cryptocurrency, e);
            // Возвращаем базовую аналитику
            return createBasicAnalytics(cryptocurrency, timeRange);
        }
    }

    /**
     * Получает настроение рынка за период.
     */
    public String getMarketSentiment(String cryptocurrency, String timeRange) {
        logger.info("Getting market sentiment for {} over {}", cryptocurrency, timeRange);
        
        try {
            return perplexityClient.analyzeMarketSentiment(cryptocurrency, timeRange);
        } catch (Exception e) {
            logger.error("Error getting market sentiment for {}", cryptocurrency, e);
            return "Unable to retrieve market sentiment analysis at this time.";
        }
    }

    /**
     * Сравнивает криптовалюты.
     */
    public String compareCryptocurrencies(List<String> cryptocurrencies) {
        logger.info("Comparing cryptocurrencies: {}", cryptocurrencies);
        
        if (cryptocurrencies == null || cryptocurrencies.isEmpty()) {
            return "No cryptocurrencies provided for comparison.";
        }
        
        try {
            return perplexityClient.compareCryptocurrencies(cryptocurrencies);
        } catch (Exception e) {
            logger.error("Error comparing cryptocurrencies: {}", cryptocurrencies, e);
            return "Unable to perform cryptocurrency comparison at this time.";
        }
    }

    /**
     * Получает позитивные новости.
     */
    public List<NewsItem> getPositiveNews(String cryptocurrency, Integer limit) {
        logger.info("Getting positive news for {}", cryptocurrency);
        
        try {
            // Сначала пытаемся получить из базы
            List<NewsItem> positiveNews = newsRepository.findPositiveNewsByCryptocurrency(cryptocurrency);
            
            if (positiveNews.isEmpty()) {
                // Получаем свежие новости и фильтруем
                List<NewsItem> allNews = getLatestCryptoNews(cryptocurrency, maxArticlesPerRequest);
                positiveNews = allNews.stream()
                        .filter(news -> news.getSentimentScore() != null && news.getSentimentScore().isPositive())
                        .collect(Collectors.toList());
            }
            
            int resultLimit = limit != null ? limit : 10;
            return positiveNews.stream().limit(resultLimit).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error getting positive news for {}", cryptocurrency, e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает негативные новости.
     */
    public List<NewsItem> getNegativeNews(String cryptocurrency, Integer limit) {
        logger.info("Getting negative news for {}", cryptocurrency);
        
        try {
            // Сначала пытаемся получить из базы
            List<NewsItem> negativeNews = newsRepository.findNegativeNewsByCryptocurrency(cryptocurrency);
            
            if (negativeNews.isEmpty()) {
                // Получаем свежие новости и фильтруем
                List<NewsItem> allNews = getLatestCryptoNews(cryptocurrency, maxArticlesPerRequest);
                negativeNews = allNews.stream()
                        .filter(news -> news.getSentimentScore() != null && news.getSentimentScore().isNegative())
                        .collect(Collectors.toList());
            }
            
            int resultLimit = limit != null ? limit : 10;
            return negativeNews.stream().limit(resultLimit).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error getting negative news for {}", cryptocurrency, e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает прогноз трендов.
     */
    public String getTrendForecast(String cryptocurrency) {
        logger.info("Getting trend forecast for {}", cryptocurrency);
        
        try {
            return perplexityClient.getTrendForecast(cryptocurrency);
        } catch (Exception e) {
            logger.error("Error getting trend forecast for {}", cryptocurrency, e);
            return "Unable to generate trend forecast at this time.";
        }
    }

    /**
     * Ищет новости по ключевым словам.
     */
    public List<NewsItem> searchCryptoNews(String cryptocurrency, String keywords) {
        logger.info("Searching crypto news for {} with keywords: {}", cryptocurrency, keywords);
        
        try {
            // Ищем в базе данных
            List<NewsItem> dbResults = newsRepository.searchByKeyword(cryptocurrency, keywords);
            
            // Получаем также свежие результаты от Perplexity
            List<String> newsTexts = perplexityClient.searchCryptoNews(cryptocurrency, keywords);
            List<NewsItem> freshResults = newsTexts.stream()
                    .map(text -> createNewsItem(text, cryptocurrency))
                    .collect(Collectors.toList());
            
            // Объединяем результаты
            Set<NewsItem> combinedResults = new HashSet<>(dbResults);
            combinedResults.addAll(freshResults);
            
            return new ArrayList<>(combinedResults);
            
        } catch (Exception e) {
            logger.error("Error searching crypto news for {} with keywords {}", cryptocurrency, keywords, e);
            return Collections.emptyList();
        }
    }

    /**
     * Получает события, влияющие на рынок.
     */
    public String getMarketMovingEvents(String cryptocurrency) {
        logger.info("Getting market moving events for {}", cryptocurrency);
        
        try {
            return perplexityClient.getMarketMovingEvents(cryptocurrency);
        } catch (Exception e) {
            logger.error("Error getting market moving events for {}", cryptocurrency, e);
            return "Unable to identify market moving events at this time.";
        }
    }

    /**
     * Анализирует корреляцию настроения и цены.
     */
    public String analyzeSentimentPriceCorrelation(String cryptocurrency) {
        logger.info("Analyzing sentiment-price correlation for {}", cryptocurrency);
        
        try {
            return perplexityClient.analyzeSentimentPriceCorrelation(cryptocurrency);
        } catch (Exception e) {
            logger.error("Error analyzing sentiment-price correlation for {}", cryptocurrency, e);
            return "Unable to analyze sentiment-price correlation at this time.";
        }
    }

    // Вспомогательные методы

    /**
     * Создает объект новости из текста.
     */
    private NewsItem createNewsItem(String newsText, String cryptocurrency) {
        NewsItem newsItem = new NewsItem();
        
        // Парсим заголовок и описание
        String[] parts = newsText.split("\n", 2);
        String title = parts[0].trim();
        String description = parts.length > 1 ? parts[1].trim() : "";
        
        // Ограничиваем длину
        if (title.length() > 500) {
            title = title.substring(0, 500) + "...";
        }
        if (description.length() > 2000) {
            description = description.substring(0, 2000) + "...";
        }
        
        newsItem.setTitle(title);
        newsItem.setDescription(description);
        newsItem.setCryptocurrency(cryptocurrency);
        newsItem.setPublishedDate(LocalDateTime.now());
        newsItem.setSource("Perplexity API");
        
        // Анализируем настроение
        String fullText = title + " " + description;
        SentimentScore sentimentScore = sentimentAnalyzer.analyzeSentiment(fullText);
        newsItem.setSentimentScore(sentimentScore);
        
        return newsItem;
    }

    /**
     * Вычисляет время начала для заданного диапазона.
     */
    private LocalDateTime calculateStartTime(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        
        if (timeRange == null || timeRange.isEmpty()) {
            return now.minusHours(defaultTimeRangeHours);
        }
        
        String lowerTimeRange = timeRange.toLowerCase();
        if (lowerTimeRange.contains("hour")) {
            int hours = extractNumber(lowerTimeRange, defaultTimeRangeHours);
            return now.minusHours(hours);
        } else if (lowerTimeRange.contains("day")) {
            int days = extractNumber(lowerTimeRange, 1);
            return now.minusDays(days);
        } else if (lowerTimeRange.contains("week")) {
            int weeks = extractNumber(lowerTimeRange, 1);
            return now.minusWeeks(weeks);
        } else {
            return now.minusHours(defaultTimeRangeHours);
        }
    }

    /**
     * Извлекает число из строки.
     */
    private int extractNumber(String text, int defaultValue) {
        try {
            String numbers = text.replaceAll("\\D+", "");
            return numbers.isEmpty() ? defaultValue : Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Извлекает ключевые темы из новостей.
     */
    private List<String> extractKeyTopics(List<NewsItem> newsItems) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        Set<String> stopWords = Set.of("the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "will", "would", "could", "should", "may", "might", "must", "can", "cryptocurrency", "crypto", "bitcoin", "ethereum");
        
        for (NewsItem news : newsItems) {
            String text = (news.getTitle() + " " + news.getDescription()).toLowerCase();
            String[] words = text.split("\s+");
            
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                if (word.length() > 3 && !stopWords.contains(word)) {
                    wordFrequency.merge(word, 1, Integer::sum);
                }
            }
        }
        
        return wordFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Вычисляет тренд настроения.
     */
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
        
        // Вычисляем средние для первой и второй половины
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

    /**
     * Создает базовую аналитику в случае ошибки.
     */
    private CryptoAnalytics createBasicAnalytics(String cryptocurrency, String timeRange) {
        return new CryptoAnalytics.Builder(cryptocurrency, timeRange)
                .totalArticles(0)
                .sentimentCounts(0, 0, 0)
                .averageSentiment(0.0)
                .sentimentTrend(0.0)
                .keyTopics(Collections.emptyList())
                .topPositiveNews(Collections.emptyList())
                .topNegativeNews(Collections.emptyList())
                .marketMovingEvents("Unable to retrieve market moving events.")
                .trendForecast("Unable to generate trend forecast.")
                .build();
    }
}