package com.toxicbert.toxic_bert_gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class TranscriptChunk {

    private double start;
    private double end;
    private String text;

    @JsonProperty("translated_text")
    private String translatedText;

    @JsonProperty("was_translated")
    private boolean wasTranslated;

    private Map<String, Double> scores;

    @JsonProperty("is_toxic")
    private boolean isToxic;

    public TranscriptChunk() {}

    public double getStart() { return start; }
    public void setStart(double start) { this.start = start; }

    public double getEnd() { return end; }
    public void setEnd(double end) { this.end = end; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public boolean isWasTranslated() { return wasTranslated; }
    public void setWasTranslated(boolean wasTranslated) { this.wasTranslated = wasTranslated; }

    public Map<String, Double> getScores() { return scores; }
    public void setScores(Map<String, Double> scores) { this.scores = scores; }

    public boolean isToxic() { return isToxic; }
    public void setToxic(boolean toxic) { isToxic = toxic; }
}