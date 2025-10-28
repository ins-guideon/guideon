package com.guideon.model;

import java.util.List;

/**
 * 하이브리드 검색 결과
 * Vector Search + BM25 Search 통합 결과
 */
public class HybridSearchResult {
    private final List<ScoredSegment> segments;
    private final int vectorResultCount;
    private final int bm25ResultCount;
    private final int fusedResultCount;
    private final long searchTimeMs;

    public HybridSearchResult(
            List<ScoredSegment> segments,
            int vectorResultCount,
            int bm25ResultCount,
            int fusedResultCount,
            long searchTimeMs) {
        this.segments = segments;
        this.vectorResultCount = vectorResultCount;
        this.bm25ResultCount = bm25ResultCount;
        this.fusedResultCount = fusedResultCount;
        this.searchTimeMs = searchTimeMs;
    }

    public List<ScoredSegment> getSegments() {
        return segments;
    }

    public int getVectorResultCount() {
        return vectorResultCount;
    }

    public int getBm25ResultCount() {
        return bm25ResultCount;
    }

    public int getFusedResultCount() {
        return fusedResultCount;
    }

    public long getSearchTimeMs() {
        return searchTimeMs;
    }

    @Override
    public String toString() {
        return "HybridSearchResult{" +
                "totalSegments=" + segments.size() +
                ", vectorResultCount=" + vectorResultCount +
                ", bm25ResultCount=" + bm25ResultCount +
                ", fusedResultCount=" + fusedResultCount +
                ", searchTimeMs=" + searchTimeMs +
                '}';
    }
}
