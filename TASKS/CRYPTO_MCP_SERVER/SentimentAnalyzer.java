package com.cryptonews.mcpserver.service;

import com.cryptonews.mcpserver.model.SentimentScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для анализа настроений криптовалютных новостей.
 * Использует словарный подход с весовыми коэффициентами для криптовалютной терминологии.
 */
@Service
public class SentimentAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalyzer.class);

    // Словари для анализа настроений
    private final Map<String, Double> positiveWords;
    private final Map<String, Double> negativeWords;
    private final Map<String, Double> cryptoPositiveTerms;
    private final Map<String, Double> cryptoNegativeTerms;
    private final Set<String> intensifiers;
    private final Set<String> negators;

    public SentimentAnalyzer() {
        this.positiveWords = initializePositiveWords();
        this.negativeWords = initializeNegativeWords();
        this.cryptoPositiveTerms = initializeCryptoPositiveTerms();
        this.cryptoNegativeTerms = initializeCryptoNegativeTerms();
        this.intensifiers = initializeIntensifiers();
        this.negators = initializeNegators();
    }

    /**
     * Анализирует настроение текста.
     */
    @Cacheable(value = "sentiment-cache", key = "#text.hashCode()")
    public SentimentScore analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentScore(0.0, 0.0, 1.0, 0.0);
        }

        logger.debug("Analyzing sentiment for text: {}", text.substring(0, Math.min(100, text.length())));

        // Предобработка текста
        String processedText = preprocessText(text);
        String[] words = processedText.split("\\s+");

        double positiveScore = 0.0;
        double negativeScore = 0.0;
        double neutralScore = 0.0;
        
        // Анализируем каждое слово с учетом контекста
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            double intensity = 1.0;
            boolean negated = false;

            // Проверяем наличие интенсификаторов и отрицаний
            if (i > 0) {
                String prevWord = words[i - 1];
                if (intensifiers.contains(prevWord)) {
                    intensity = 1.5;
                }
                if (negators.contains(prevWord)) {
                    negated = true;
                }
            }

            // Анализируем слово
            Double score = getWordSentiment(word);
            if (score != null) {
                if (negated) {
                    score = -score; // Инвертируем при отрицании
                }
                score *= intensity;

                if (score > 0) {
                    positiveScore += score;
                } else if (score < 0) {
                    negativeScore += Math.abs(score);
                }
            }
        }

        // Нормализуем счета
        double totalWords = words.length;
        positiveScore = Math.min(positiveScore / totalWords, 1.0);
        negativeScore = Math.min(negativeScore / totalWords, 1.0);
        neutralScore = Math.max(0.0, 1.0 - positiveScore - negativeScore);

        // Вычисляем составной счет
        double compoundScore = positiveScore - negativeScore;
        
        // Нормализуем составной счет в диапазон [-1, 1]
        compoundScore = Math.max(-1.0, Math.min(1.0, compoundScore));

        return new SentimentScore(positiveScore, negativeScore, neutralScore, compoundScore);
    }

    /**
     * Анализирует несколько текстов и возвращает агрегированный результат.
     */
    public SentimentScore analyzeMultipleTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new SentimentScore(0.0, 0.0, 1.0, 0.0);
        }

        double totalPositive = 0.0;
        double totalNegative = 0.0;
        double totalNeutral = 0.0;
        double totalCompound = 0.0;

        for (String text : texts) {
            SentimentScore score = analyzeSentiment(text);
            totalPositive += score.getPositiveScore();
            totalNegative += score.getNegativeScore();
            totalNeutral += score.getNeutralScore();
            totalCompound += score.getCompoundScore();
        }

        int count = texts.size();
        return new SentimentScore(
                totalPositive / count,
                totalNegative / count,
                totalNeutral / count,
                totalCompound / count
        );
    }

    /**
     * Предобрабатывает текст для анализа.
     */
    private String preprocessText(String text) {
        // Приводим к нижнему регистру
        text = text.toLowerCase();
        
        // Удаляем URL и упоминания
        text = text.replaceAll("https?://\\S+", "");
        text = text.replaceAll("@\\w+", "");
        text = text.replaceAll("#\\w+", "");
        
        // Удаляем знаки препинания, кроме важных
        text = text.replaceAll("[^a-zA-Z0-9\\s\\-\\$%]", " ");
        
        // Удаляем множественные пробелы
        text = text.replaceAll("\\s+", " ");
        
        return text.trim();
    }

    /**
     * Получает эмоциональную оценку слова.
     */
    private Double getWordSentiment(String word) {
        // Сначала проверяем криптовалютные термины
        if (cryptoPositiveTerms.containsKey(word)) {
            return cryptoPositiveTerms.get(word);
        }
        if (cryptoNegativeTerms.containsKey(word)) {
            return -cryptoNegativeTerms.get(word);
        }
        
        // Затем общие слова
        if (positiveWords.containsKey(word)) {
            return positiveWords.get(word);
        }
        if (negativeWords.containsKey(word)) {
            return -negativeWords.get(word);
        }
        
        return null;
    }

    /**
     * Инициализирует словарь позитивных слов.
     */
    private Map<String, Double> initializePositiveWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("good", 0.5);
        words.put("great", 0.7);
        words.put("excellent", 0.8);
        words.put("amazing", 0.8);
        words.put("awesome", 0.8);
        words.put("fantastic", 0.8);
        words.put("wonderful", 0.7);
        words.put("positive", 0.6);
        words.put("strong", 0.6);
        words.put("success", 0.7);
        words.put("successful", 0.7);
        words.put("win", 0.6);
        words.put("winning", 0.6);
        words.put("profit", 0.7);
        words.put("profitable", 0.7);
        words.put("growth", 0.6);
        words.put("growing", 0.6);
        words.put("increase", 0.5);
        words.put("rising", 0.6);
        words.put("surge", 0.8);
        words.put("boom", 0.8);
        words.put("breakthrough", 0.8);
        words.put("innovation", 0.6);
        words.put("revolutionary", 0.8);
        words.put("promising", 0.6);
        words.put("optimistic", 0.7);
        words.put("confidence", 0.6);
        words.put("confident", 0.6);
        return words;
    }

    /**
     * Инициализирует словарь негативных слов.
     */
    private Map<String, Double> initializeNegativeWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("bad", 0.5);
        words.put("terrible", 0.8);
        words.put("awful", 0.8);
        words.put("horrible", 0.8);
        words.put("negative", 0.6);
        words.put("weak", 0.6);
        words.put("failure", 0.7);
        words.put("fail", 0.7);
        words.put("loss", 0.7);
        words.put("losing", 0.7);
        words.put("decline", 0.6);
        words.put("falling", 0.6);
        words.put("drop", 0.6);
        words.put("crash", 0.9);
        words.put("collapse", 0.9);
        words.put("plunge", 0.8);
        words.put("plummet", 0.8);
        words.put("disaster", 0.9);
        words.put("crisis", 0.8);
        words.put("concern", 0.5);
        words.put("worried", 0.6);
        words.put("fear", 0.7);
        words.put("panic", 0.8);
        words.put("risk", 0.5);
        words.put("risky", 0.6);
        words.put("dangerous", 0.7);
        words.put("volatile", 0.6);
        words.put("uncertainty", 0.6);
        words.put("doubt", 0.6);
        return words;
    }

    /**
     * Инициализирует словарь криптовалютных позитивных терминов.
     */
    private Map<String, Double> initializeCryptoPositiveTerms() {
        Map<String, Double> terms = new HashMap<>();
        terms.put("moon", 0.9);
        terms.put("mooning", 0.9);
        terms.put("lambo", 0.8);
        terms.put("hodl", 0.7);
        terms.put("diamond", 0.8);
        terms.put("hands", 0.6);
        terms.put("pump", 0.8);
        terms.put("pumping", 0.8);
        terms.put("bull", 0.7);
        terms.put("bullish", 0.8);
        terms.put("rally", 0.7);
        terms.put("ath", 0.8); // All Time High
        terms.put("adoption", 0.7);
        terms.put("mainstream", 0.6);
        terms.put("institutional", 0.6);
        terms.put("partnership", 0.6);
        terms.put("upgrade", 0.6);
        terms.put("fork", 0.5);
        terms.put("staking", 0.5);
        terms.put("yield", 0.6);
        terms.put("defi", 0.6);
        terms.put("nft", 0.5);
        terms.put("web3", 0.6);
        terms.put("blockchain", 0.5);
        terms.put("decentralized", 0.6);
        terms.put("launch", 0.6);
        terms.put("listing", 0.7);
        terms.put("accumulate", 0.6);
        terms.put("buy", 0.5);
        terms.put("long", 0.6);
        return terms;
    }

    /**
     * Инициализирует словарь криптовалютных негативных терминов.
     */
    private Map<String, Double> initializeCryptoNegativeTerms() {
        Map<String, Double> terms = new HashMap<>();
        terms.put("dump", 0.8);
        terms.put("dumping", 0.8);
        terms.put("bear", 0.7);
        terms.put("bearish", 0.8);
        terms.put("rekt", 0.9);
        terms.put("liquidation", 0.8);
        terms.put("liquidated", 0.8);
        terms.put("fud", 0.8);
        terms.put("paperhands", 0.7);
        terms.put("sell", 0.5);
        terms.put("selling", 0.5);
        terms.put("short", 0.6);
        terms.put("correction", 0.6);
        terms.put("dip", 0.5);
        terms.put("flash", 0.6); // flash crash
        terms.put("rug", 0.9); // rug pull
        terms.put("scam", 0.9);
        terms.put("hack", 0.8);
        terms.put("hacked", 0.8);
        terms.put("exploit", 0.8);
        terms.put("bug", 0.6);
        terms.put("delay", 0.5);
        terms.put("postpone", 0.5);
        terms.put("regulation", 0.6);
        terms.put("ban", 0.8);
        terms.put("banned", 0.8);
        terms.put("illegal", 0.8);
        terms.put("warning", 0.6);
        terms.put("investigation", 0.7);
        terms.put("fine", 0.7);
        terms.put("lawsuit", 0.8);
        return terms;
    }

    /**
     * Инициализирует интенсификаторы.
     */
    private Set<String> initializeIntensifiers() {
        return Set.of("very", "extremely", "highly", "super", "really", "absolutely", 
                     "completely", "totally", "massively", "significantly");
    }

    /**
     * Инициализирует отрицания.
     */
    private Set<String> initializeNegators() {
        return Set.of("not", "no", "never", "none", "nothing", "neither", "nobody", 
                     "nowhere", "hardly", "rarely", "seldom", "without");
    }
}