package com.toxicbert.toxic_bert_gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AnalyzeResult {

    private String text;
    private Map<String, Double> scores;

    @JsonProperty("is_toxic")
    private boolean isToxic;

    @JsonProperty("max_label")
    private String maxLabel;

    @JsonProperty("max_score")
    private double maxScore;

    @JsonProperty("latency_ms")
    private double latencyMs;

    @JsonProperty("original_text")
    private String originalText;

    @JsonProperty("translated_text")
    private String translatedText;

    @JsonProperty("was_translated")
    private boolean wasTranslated;

    public AnalyzeResult() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Map<String, Double> getScores() { return scores; }
    public void setScores(Map<String, Double> scores) { this.scores = scores; }

    public boolean isToxic() { return isToxic; }
    public void setToxic(boolean toxic) { isToxic = toxic; }

    public String getMaxLabel() { return maxLabel; }
    public void setMaxLabel(String maxLabel) { this.maxLabel = maxLabel; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(double latencyMs) { this.latencyMs = latencyMs; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public boolean isWasTranslated() { return wasTranslated; }
    public void setWasTranslated(boolean wasTranslated) { this.wasTranslated = wasTranslated; }
}