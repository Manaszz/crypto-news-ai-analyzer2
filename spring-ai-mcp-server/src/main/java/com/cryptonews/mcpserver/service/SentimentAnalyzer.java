package com.cryptonews.mcpserver.service;

import com.cryptonews.mcpserver.model.SentimentScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class SentimentAnalyzer {

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

    @Cacheable(value = "sentiment-cache", key = "#text.hashCode()")
    public SentimentScore analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentScore(0.0, 0.0, 1.0, 0.0);
        }

        log.debug("Analyzing sentiment for text: {}", text.substring(0, Math.min(100, text.length())));

        String processedText = preprocessText(text);
        String[] words = processedText.split("\\s+");

        double positiveScore = 0.0;
        double negativeScore = 0.0;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            double intensity = 1.0;
            boolean negated = false;

            if (i > 0) {
                String prevWord = words[i - 1];
                if (intensifiers.contains(prevWord)) {
                    intensity = 1.5;
                }
                if (negators.contains(prevWord)) {
                    negated = true;
                }
            }

            Double score = getWordSentiment(word);
            if (score != null) {
                if (negated) {
                    score = -score;
                }
                score *= intensity;

                if (score > 0) {
                    positiveScore += score;
                } else if (score < 0) {
                    negativeScore += Math.abs(score);
                }
            }
        }

        double totalWords = words.length > 0 ? words.length : 1;
        positiveScore = Math.min(positiveScore / totalWords, 1.0);
        negativeScore = Math.min(negativeScore / totalWords, 1.0);
        double neutralScore = Math.max(0.0, 1.0 - positiveScore - negativeScore);
        double compoundScore = Math.max(-1.0, Math.min(1.0, positiveScore - negativeScore));

        return new SentimentScore(positiveScore, negativeScore, neutralScore, compoundScore);
    }

    private String preprocessText(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("https?://\\S+", "");
        text = text.replaceAll("@[\\w-]+", "");
        text = text.replaceAll("#[\\w-]+", "");
        text = text.replaceAll("[^a-zA-Z0-9\\s\\-]", " ");
        text = text.replaceAll("\\s+", " ");
        return text.trim();
    }

    private Double getWordSentiment(String word) {
        if (cryptoPositiveTerms.containsKey(word)) return cryptoPositiveTerms.get(word);
        if (cryptoNegativeTerms.containsKey(word)) return -cryptoNegativeTerms.get(word);
        if (positiveWords.containsKey(word)) return positiveWords.get(word);
        if (negativeWords.containsKey(word)) return -negativeWords.get(word);
        return null;
    }

    private Map<String, Double> initializePositiveWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("good", 0.5); words.put("great", 0.7); words.put("excellent", 0.8);
        words.put("positive", 0.6); words.put("strong", 0.6); words.put("success", 0.7);
        words.put("profit", 0.7); words.put("growth", 0.6); words.put("increase", 0.5);
        words.put("rising", 0.6); words.put("surge", 0.8); words.put("boom", 0.8);
        words.put("promising", 0.6); words.put("optimistic", 0.7);
        return words;
    }

    private Map<String, Double> initializeNegativeWords() {
        Map<String, Double> words = new HashMap<>();
        words.put("bad", 0.5); words.put("terrible", 0.8); words.put("negative", 0.6);
        words.put("weak", 0.6); words.put("failure", 0.7); words.put("loss", 0.7);
        words.put("decline", 0.6); words.put("falling", 0.6); words.put("drop", 0.6);
        words.put("crash", 0.9); words.put("risk", 0.5); words.put("volatile", 0.6);
        words.put("fear", 0.7); words.put("panic", 0.8);
        return words;
    }

    private Map<String, Double> initializeCryptoPositiveTerms() {
        Map<String, Double> terms = new HashMap<>();
        terms.put("moon", 0.9); terms.put("lambo", 0.8); terms.put("hodl", 0.7);
        terms.put("pump", 0.8); terms.put("bullish", 0.8); terms.put("rally", 0.7);
        terms.put("ath", 0.8); terms.put("adoption", 0.7); terms.put("partnership", 0.6);
        terms.put("upgrade", 0.6); terms.put("staking", 0.5); terms.put("defi", 0.6);
        return terms;
    }

    private Map<String, Double> initializeCryptoNegativeTerms() {
        Map<String, Double> terms = new HashMap<>();
        terms.put("dump", 0.8); terms.put("bearish", 0.8); terms.put("rekt", 0.9);
        terms.put("fud", 0.8); terms.put("sell", 0.5); terms.put("correction", 0.6);
        terms.put("dip", 0.5); terms.put("rug", 0.9); terms.put("scam", 0.9);
        terms.put("hack", 0.8); terms.put("regulation", 0.6); terms.put("ban", 0.8);
        return terms;
    }

    private Set<String> initializeIntensifiers() {
        return Set.of("very", "extremely", "highly", "super", "really", "massively", "significantly");
    }

    private Set<String> initializeNegators() {
        return Set.of("not", "no", "never", "none", "without");
    }
} 