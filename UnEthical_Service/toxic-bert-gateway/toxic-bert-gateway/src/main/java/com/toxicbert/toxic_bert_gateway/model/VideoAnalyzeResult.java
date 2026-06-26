package com.toxicbert.toxic_bert_gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class VideoAnalyzeResult {

    private String transcript;
    private List<TranscriptChunk> chunks;
    private Map<String, Double> scores;

    @JsonProperty("is_toxic")
    private boolean isToxic;

    @JsonProperty("max_label")
    private String maxLabel;

    @JsonProperty("max_score")
    private double maxScore;

    @JsonProperty("translated_text")
    private String translatedText;

    @JsonProperty("was_translated")
    private boolean wasTranslated;

    @JsonProperty("whisper_ms")
    private double whisperMs;

    public VideoAnalyzeResult() {}

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public List<TranscriptChunk> getChunks() { return chunks; }
    public void setChunks(List<TranscriptChunk> chunks) { this.chunks = chunks; }

    public Map<String, Double> getScores() { return scores; }
    public void setScores(Map<String, Double> scores) { this.scores = scores; }

    public boolean isToxic() { return isToxic; }
    public void setToxic(boolean toxic) { isToxic = toxic; }

    public String getMaxLabel() { return maxLabel; }
    public void setMaxLabel(String maxLabel) { this.maxLabel = maxLabel; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public boolean isWasTranslated() { return wasTranslated; }
    public void setWasTranslated(boolean wasTranslated) { this.wasTranslated = wasTranslated; }

    public double getWhisperMs() { return whisperMs; }
    public void setWhisperMs(double whisperMs) { this.whisperMs = whisperMs; }
}