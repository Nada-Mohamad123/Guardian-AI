package com.toxicbert.toxic_bert_gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AudioChunkResult {

    private String transcript;

    @JsonProperty("translated_text")
    private String translatedText;

    @JsonProperty("was_translated")
    private boolean wasTranslated;

    @JsonProperty("is_toxic")
    private boolean isToxic;

    @JsonProperty("max_label")
    private String maxLabel;

    @JsonProperty("max_score")
    private double maxScore;

    private Map<String, Double> scores;
    private String category;
    private double confidence;
    private String error;

    public AudioChunkResult() {}

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public boolean isWasTranslated() { return wasTranslated; }
    public void setWasTranslated(boolean wasTranslated) { this.wasTranslated = wasTranslated; }

    public boolean isToxic() { return isToxic; }
    public void setToxic(boolean toxic) { isToxic = toxic; }

    public String getMaxLabel() { return maxLabel; }
    public void setMaxLabel(String maxLabel) { this.maxLabel = maxLabel; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public Map<String, Double> getScores() { return scores; }
    public void setScores(Map<String, Double> scores) { this.scores = scores; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}