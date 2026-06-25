package com.toxicbert.toxic_bert_gateway.model;

import java.util.List;

public class BatchAnalyzeResult {

    private List<AnalyzeResult> results;

    public BatchAnalyzeResult() {}

    public List<AnalyzeResult> getResults() { return results; }
    public void setResults(List<AnalyzeResult> results) { this.results = results; }
}