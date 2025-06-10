package com.cryptonews.mcpserver.model;

import jakarta.persistence.Embeddable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentScore {

    private double positiveScore;
    private double negativeScore;
    private double neutralScore;
    private double compoundScore;
    private String polarity;
    private double subjectivity;

    public enum SentimentLabel {
        POSITIVE, NEGATIVE, NEUTRAL
    }
    
    public SentimentScore(double positiveScore, double negativeScore, double neutralScore, double compoundScore) {
        this.positiveScore = positiveScore;
        this.negativeScore = negativeScore;
        this.neutralScore = neutralScore;
        this.compoundScore = compoundScore;
    }

    public double getPositiveScore() {
        return positiveScore;
    }

    public void setPositiveScore(double positiveScore) {
        this.positiveScore = positiveScore;
    }

    public double getNegativeScore() {
        return negativeScore;
    }

    public void setNegativeScore(double negativeScore) {
        this.negativeScore = negativeScore;
    }

    public double getNeutralScore() {
        return neutralScore;
    }

    public void setNeutralScore(double neutralScore) {
        this.neutralScore = neutralScore;
    }

    public double getCompoundScore() {
        return compoundScore;
    }

    public void setCompoundScore(double compoundScore) {
        this.compoundScore = compoundScore;
    }
    
    @JsonIgnore
    public SentimentLabel getLabel() {
        if (compoundScore >= 0.05) {
            return SentimentLabel.POSITIVE;
        } else if (compoundScore <= -0.05) {
            return SentimentLabel.NEGATIVE;
        } else {
            return SentimentLabel.NEUTRAL;
        }
    }

    @JsonIgnore
    public boolean isPositive() {
        return getLabel() == SentimentLabel.POSITIVE;
    }

    @JsonIgnore
    public boolean isNegative() {
        return getLabel() == SentimentLabel.NEGATIVE;
    }

    public String getPolarity() {
        return polarity;
    }

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    public double getScore() {
        return compoundScore;
    }

    public void setSubjectivity(double subjectivity) {
        this.subjectivity = subjectivity;
    }
} 