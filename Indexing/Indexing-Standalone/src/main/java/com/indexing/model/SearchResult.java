package com.indexing.model;

import java.util.Map;

/**
 * Represents a search result with document, relevance score, and context.
 */
public class SearchResult implements Comparable<SearchResult> {
    private Document document;
    private double score;
    private String scoreType; // "similarity", "relevance", "keyword_count"
    private Map<String, Object> metadata;

    public SearchResult(Document document, double score) {
        this(document, score, "similarity", null);
    }

    public SearchResult(Document document, double score, String scoreType, Map<String, Object> metadata) {
        this.document = document;
        this.score = score;
        this.scoreType = scoreType;
        this.metadata = metadata;
    }

    public Document getDocument() {
        return document;
    }

    public double getScore() {
        return score;
    }

    public String getScoreType() {
        return scoreType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public int compareTo(SearchResult other) {
        // Higher scores first (descending order)
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "score=" + String.format("%.4f", score) +
                ", scoreType='" + scoreType + '\'' +
                ", docId='" + document.getNodeId() + '\'' +
                '}';
    }
}
