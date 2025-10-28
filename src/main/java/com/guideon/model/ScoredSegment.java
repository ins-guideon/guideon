package com.guideon.model;

import dev.langchain4j.data.segment.TextSegment;

/**
 * 검색 점수를 포함한 텍스트 세그먼트
 * BM25, Vector Search, Hybrid Search 결과를 통합 표현
 */
public class ScoredSegment {
    private final String id;
    private final TextSegment segment;
    private final double score;
    private final String source; // "VECTOR", "BM25", "HYBRID"

    public ScoredSegment(String id, TextSegment segment, double score, String source) {
        this.id = id;
        this.segment = segment;
        this.score = score;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public TextSegment getSegment() {
        return segment;
    }

    public double getScore() {
        return score;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "ScoredSegment{" +
                "id='" + id + '\'' +
                ", score=" + String.format("%.3f", score) +
                ", source='" + source + '\'' +
                ", content='" + (segment != null ? segment.text().substring(0, Math.min(50, segment.text().length())) + "..." : "null") + '\'' +
                '}';
    }
}
