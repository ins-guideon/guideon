package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.HybridSearchResult;
import com.guideon.model.ScoredSegment;
import com.guideon.util.RRFCalculator;
import com.guideon.util.SearchResultConverter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 하이브리드 검색 서비스
 * Vector Search (의미 검색) + BM25 Search (키워드 검색)를 통합
 */
public class HybridSearchService {
    private static final Logger logger = LoggerFactory.getLogger(HybridSearchService.class);

    private final BM25SearchService bm25SearchService;
    private EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingService embeddingService;
    private final ConfigLoader config;

    private final boolean enabled;
    private final double vectorWeight;
    private final double keywordWeight;
    private final int initialResults;

    public HybridSearchService(
            BM25SearchService bm25SearchService,
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingService embeddingService,
            ConfigLoader config) {

        this.bm25SearchService = bm25SearchService;
        this.embeddingStore = embeddingStore;
        this.embeddingService = embeddingService;
        this.config = config;

        this.enabled = config.isHybridSearchEnabled();
        this.vectorWeight = config.getHybridVectorWeight();
        this.keywordWeight = config.getHybridKeywordWeight();
        this.initialResults = config.getHybridInitialResults();

        logger.info("HybridSearchService initialized");
        logger.info("Hybrid Search Enabled: {}", enabled);
        logger.info("Vector Weight: {}, Keyword Weight: {}", vectorWeight, keywordWeight);
        logger.info("Initial Results: {}", initialResults);
    }

    /**
     * 하이브리드 검색 실행
     * Vector Search와 BM25 Search를 병렬로 실행하고 RRF로 통합
     *
     * @param query 검색 쿼리
     * @param maxResults 최종 결과 수
     * @return 하이브리드 검색 결과
     */
    public HybridSearchResult search(String query, int maxResults) {
        if (!enabled) {
            logger.warn("Hybrid Search is disabled. Falling back to Vector Search only.");
            return performVectorSearchOnly(query, maxResults);
        }

        long startTime = System.currentTimeMillis();

        try {
            // 병렬로 Vector Search와 BM25 Search 실행
            CompletableFuture<List<ScoredSegment>> vectorFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return performVectorSearch(query, initialResults);
                } catch (Exception e) {
                    logger.error("Vector search failed", e);
                    return List.of();
                }
            });

            CompletableFuture<List<ScoredSegment>> bm25Future = CompletableFuture.supplyAsync(() -> {
                try {
                    return performBM25Search(query, initialResults);
                } catch (Exception e) {
                    logger.error("BM25 search failed", e);
                    return List.of();
                }
            });

            // 두 검색 결과를 기다림
            List<ScoredSegment> vectorResults = vectorFuture.get();
            List<ScoredSegment> bm25Results = bm25Future.get();

            logger.info("Vector Search returned {} results", vectorResults.size());
            logger.info("BM25 Search returned {} results", bm25Results.size());

            // RRF를 사용하여 결과 통합
            List<ScoredSegment> fusedResults = fuseResults(vectorResults, bm25Results, maxResults);

            long searchTime = System.currentTimeMillis() - startTime;

            logger.info("Hybrid Search completed in {}ms, returned {} results", searchTime, fusedResults.size());

            return new HybridSearchResult(
                    fusedResults,
                    vectorResults.size(),
                    bm25Results.size(),
                    fusedResults.size(),
                    searchTime
            );

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Hybrid search execution failed", e);
            long searchTime = System.currentTimeMillis() - startTime;

            // 실패 시 Vector Search로 폴백
            logger.warn("Falling back to Vector Search only");
            return performVectorSearchOnly(query, maxResults);
        }
    }

    /**
     * Vector Search 수행
     *
     * @param query 검색 쿼리
     * @param maxResults 최대 결과 수
     * @return Vector Search 결과
     */
    private List<ScoredSegment> performVectorSearch(String query, int maxResults) {
        logger.debug("Performing Vector Search for: {}", query);

        // 쿼리 임베딩 생성
        var queryEmbedding = embeddingService.embed(query);

        // Vector Store에서 검색
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding.content(),
                maxResults
        );

        // EmbeddingMatch를 ScoredSegment로 변환
        return matches.stream()
                .map(SearchResultConverter::toScoredSegment)
                .collect(Collectors.toList());
    }

    /**
     * BM25 Search 수행
     *
     * @param query 검색 쿼리
     * @param maxResults 최대 결과 수
     * @return BM25 Search 결과
     */
    private List<ScoredSegment> performBM25Search(String query, int maxResults) {
        logger.debug("Performing BM25 Search for: {}", query);

        try {
            return bm25SearchService.search(query, maxResults);
        } catch (Exception e) {
            logger.error("BM25 search failed", e);
            return List.of();
        }
    }

    /**
     * RRF를 사용하여 Vector Search와 BM25 Search 결과 통합
     *
     * @param vectorResults Vector Search 결과
     * @param bm25Results BM25 Search 결과
     * @param maxResults 최종 결과 수
     * @return 통합된 검색 결과
     */
    private List<ScoredSegment> fuseResults(
            List<ScoredSegment> vectorResults,
            List<ScoredSegment> bm25Results,
            int maxResults) {

        logger.debug("Fusing results with RRF (vectorWeight: {}, keywordWeight: {})", vectorWeight, keywordWeight);

        // RRF 점수 계산
        Map<String, Double> fusionScores = RRFCalculator.fuseTwoResults(
                vectorResults,
                bm25Results,
                vectorWeight,
                keywordWeight
        );

        // ID -> ScoredSegment 매핑 생성 (원본 데이터 보존)
        Map<String, ScoredSegment> segmentMap = new HashMap<>();

        for (ScoredSegment segment : vectorResults) {
            segmentMap.put(segment.getId(), segment);
        }

        for (ScoredSegment segment : bm25Results) {
            segmentMap.putIfAbsent(segment.getId(), segment);
        }

        // RRF 점수로 정렬하고 최종 결과 반환
        List<ScoredSegment> fusedResults = RRFCalculator.sortAndLimitResults(
                fusionScores,
                segmentMap,
                maxResults
        );

        logger.debug("Fusion completed: {} unique results", fusedResults.size());

        return fusedResults;
    }

    /**
     * Vector Search만 사용 (폴백)
     *
     * @param query 검색 쿼리
     * @param maxResults 최대 결과 수
     * @return Vector Search 결과
     */
    private HybridSearchResult performVectorSearchOnly(String query, int maxResults) {
        long startTime = System.currentTimeMillis();

        List<ScoredSegment> vectorResults = performVectorSearch(query, maxResults);

        long searchTime = System.currentTimeMillis() - startTime;

        return new HybridSearchResult(
                vectorResults,
                vectorResults.size(),
                0,
                vectorResults.size(),
                searchTime
        );
    }

    /**
     * Hybrid Search 활성화 여부 확인
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * BM25 인덱스에 세그먼트 추가
     *
     * @param segment 텍스트 세그먼트
     * @param regulationType 규정 유형
     * @param segmentId 세그먼트 ID
     */
    public void indexSegment(TextSegment segment, String regulationType, String segmentId) {
        if (!enabled) {
            logger.debug("Hybrid Search disabled, skipping BM25 indexing");
            return;
        }

        try {
            bm25SearchService.indexSegment(segment, regulationType, segmentId);
            logger.debug("Segment indexed: {} ({})", segmentId, regulationType);
        } catch (Exception e) {
            logger.error("Failed to index segment: {}", segmentId, e);
        }
    }

    /**
     * BM25 인덱스 커밋 (변경사항 디스크에 저장)
     */
    public void commit() {
        if (!enabled) {
            return;
        }

        try {
            bm25SearchService.commit();
            logger.info("BM25 index committed");
        } catch (Exception e) {
            logger.error("Failed to commit BM25 index", e);
        }
    }

    /**
     * 특정 문서의 모든 세그먼트 삭제
     * BM25 인덱스에서 해당 document_id를 가진 모든 세그먼트를 삭제합니다.
     *
     * @param documentId 삭제할 문서 ID
     * @return 삭제된 세그먼트 수
     */
    public int deleteDocumentSegments(String documentId) {
        if (!enabled) {
            logger.debug("Hybrid Search disabled, skipping BM25 segment deletion");
            return 0;
        }

        try {
            int deletedCount = bm25SearchService.deleteDocumentSegments(documentId);
            logger.info("Deleted {} BM25 segments for document: {}", deletedCount, documentId);
            return deletedCount;
        } catch (Exception e) {
            logger.error("Failed to delete BM25 segments for document: {}", documentId, e);
            throw new RuntimeException("BM25 세그먼트 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * EmbeddingStore 설정 (RegulationSearchService와 동일한 스토어 공유)
     *
     * @param embeddingStore 공유할 EmbeddingStore
     */
    public void setEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingStore = embeddingStore;
        logger.info("EmbeddingStore updated for HybridSearchService");
    }

    /**
     * 서비스 종료 시 리소스 정리
     */
    public void close() {
        if (bm25SearchService != null) {
            try {
                bm25SearchService.close();
                logger.info("HybridSearchService closed");
            } catch (Exception e) {
                logger.error("Failed to close HybridSearchService", e);
            }
        }
    }
}
