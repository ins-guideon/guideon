package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.HybridSearchResult;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationReference;
import com.guideon.model.RegulationSearchResult;
import com.guideon.model.ScoredSegment;
import com.guideon.util.SearchResultConverter;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.cohere.CohereScoringModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 기반 규정 검색 및 답변 생성 서비스
 */
public class RegulationSearchService {
    private static final Logger logger = LoggerFactory.getLogger(RegulationSearchService.class);

    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final ScoringModel scoringModel;
    private final HybridSearchService hybridSearchService;

    private final int maxResults;
    private final double minScore;
    private final int chunkSize;
    private final int chunkOverlap;

    // ReRanking 설정
    private final boolean reRankingEnabled;
    private final int reRankingInitialResults;
    private final int reRankingFinalResults;
    private final double reRankingMinScore;

    // Hybrid Search 설정
    private final boolean hybridSearchEnabled;

    /**
     * application.properties 기반 생성자
     */
    public RegulationSearchService(ConfigLoader config, HybridSearchService hybridSearchService) {
        String apiKey = config.getGeminiApiKey();

        // Gemini Chat Model 초기화
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.2)
                .build();

        // Google AI Gemini Embedding Model 초기화 (한국어 지원)
        this.embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-004")
                .maxRetries(3)
                .build();

        // In-Memory Embedding Store (실제 운영시 Qdrant로 교체)
        this.embeddingStore = new InMemoryEmbeddingStore<>();

        // Hybrid Search Service
        this.hybridSearchService = hybridSearchService;
        this.hybridSearchEnabled = config.isHybridSearchEnabled();

        // Properties에서 설정값 로드
        this.maxResults = config.getVectorSearchMaxResults();
        this.minScore = config.getVectorSearchMinScore();
        this.chunkSize = config.getRagChunkSize();
        this.chunkOverlap = config.getRagChunkOverlap();

        // ReRanking 설정 로드
        this.reRankingEnabled = config.isReRankingEnabled();
        this.reRankingInitialResults = config.getReRankingInitialResults();
        this.reRankingFinalResults = config.getReRankingFinalResults();
        this.reRankingMinScore = config.getReRankingMinScore();

        logger.info("ReRanking configuration loaded: enabled={}, initialResults={}, finalResults={}, minScore={}",
                reRankingEnabled, reRankingInitialResults, reRankingFinalResults, reRankingMinScore);

        // Cohere Scoring Model 초기화 (ReRanking용)
        String cohereApiKey = config.getCohereApiKey();
        logger.info("Cohere API key configured: {}", cohereApiKey != null && !cohereApiKey.isEmpty() ? "YES" : "NO");
        if (reRankingEnabled && cohereApiKey != null && !cohereApiKey.isEmpty()) {
            this.scoringModel = CohereScoringModel.builder()
                    .apiKey(cohereApiKey)
                    .modelName(config.getReRankingModelName())
                    .build();
            logger.info("ReRanking enabled with Cohere model: {}", config.getReRankingModelName());
        } else {
            this.scoringModel = null;
            if (reRankingEnabled) {
                logger.warn("ReRanking is enabled but Cohere API key is not configured. ReRanking will be disabled.");
            }
        }

        logger.info("RegulationSearchService initialized with maxResults={}, minScore={}, chunkSize={}, chunkOverlap={}, reRankingEnabled={}, hybridSearchEnabled={}",
                maxResults, minScore, chunkSize, chunkOverlap, reRankingEnabled && scoringModel != null, hybridSearchEnabled);
    }

    /**
     * API 키 직접 전달 생성자 (하위 호환성)
     */
    @Deprecated
    public RegulationSearchService(String geminiApiKey) {
        // Gemini Chat Model 초기화
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.2)
                .build();

        // Google AI Gemini Embedding Model 초기화 (한국어 지원)
        this.embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName("text-embedding-004")
                .maxRetries(3)
                .build();

        // In-Memory Embedding Store (실제 운영시 Qdrant로 교체)
        this.embeddingStore = new InMemoryEmbeddingStore<>();

        // Hybrid Search (비활성화)
        this.hybridSearchService = null;
        this.hybridSearchEnabled = false;

        // 기본값 사용
        this.maxResults = 5;
        this.minScore = 0.5;
        this.chunkSize = 500;
        this.chunkOverlap = 100;

        // ReRanking 기본값 (비활성화)
        this.reRankingEnabled = false;
        this.reRankingInitialResults = 20;
        this.reRankingFinalResults = 5;
        this.reRankingMinScore = 0.8;
        this.scoringModel = null;

        logger.info("RegulationSearchService initialized with default values (ReRanking disabled, Hybrid Search disabled)");
    }

    /**
     * 규정 문서 인덱싱
     */
    public void indexDocument(Document document, String regulationType) {
        logger.info("Indexing document for regulation type: {}", regulationType);

        // 문서를 청크로 분할
        DocumentSplitter splitter = DocumentSplitters.recursive(
                chunkSize,      // maxSegmentSize
                chunkOverlap    // maxOverlapSize
        );

        List<TextSegment> segments = splitter.split(document);

        // 각 세그먼트에 메타데이터 추가
        for (TextSegment segment : segments) {
            segment.metadata().put("regulation_type", regulationType);
        }

        // 임베딩 생성 및 Vector Store에 저장
        int segmentIndex = 0;
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            String embeddingId = embeddingStore.add(embedding, segment);

            // Hybrid Search가 활성화된 경우 BM25 인덱스에도 추가
            if (hybridSearchEnabled && hybridSearchService != null) {
                String segmentId = embeddingId != null ? embeddingId :
                    String.format("%s_segment_%d", regulationType, segmentIndex);
                hybridSearchService.indexSegment(segment, regulationType, segmentId);
            }

            segmentIndex++;
        }

        // BM25 인덱스 커밋 (변경사항 저장)
        if (hybridSearchEnabled && hybridSearchService != null) {
            hybridSearchService.commit();
        }

        logger.info("Indexed {} segments for {} (Vector: YES, BM25: {})",
                segments.size(), regulationType, hybridSearchEnabled ? "YES" : "NO");
    }

    /**
     * 분석된 쿼리를 기반으로 규정 검색 및 답변 생성
     */
    public RegulationSearchResult search(QueryAnalysisResult analysis) {
        logger.info("Searching regulations for query: {}", analysis.getOriginalQuery());

        try {
            List<EmbeddingMatch<TextSegment>> relevantSegments;

            // Hybrid Search 활성화 여부에 따라 검색 방식 선택
            if (hybridSearchEnabled && hybridSearchService != null) {
                // Hybrid Search 수행 (Vector + BM25 + RRF)
                relevantSegments = performHybridSearch(analysis.getSearchQuery());
            } else if (reRankingEnabled && scoringModel != null) {
                // ReRanking 수행 (Vector + ReRanking)
                relevantSegments = performVectorSearchWithReRanking(analysis.getSearchQuery());
            } else {
                // 기본 Vector Search만 수행
                relevantSegments = performVectorSearch(
                        analysis.getSearchQuery(),
                        maxResults,
                        minScore
                );
                logger.info("Vector Search: Found {} results", relevantSegments.size());
            }

            // 2. 검색 결과가 없으면 Fallback 응답
            if (relevantSegments.isEmpty()) {
                return createFallbackResponse();
            }

            // 3. 검색된 세그먼트를 참조 정보로 변환
            List<RegulationReference> references = convertToReferences(relevantSegments);

            // 4. RAG 기반 답변 생성
            String answer = generateAnswer(
                    analysis.getOriginalQuery(),
                    relevantSegments
            );

            // 5. 신뢰도 점수 계산
            double confidenceScore = calculateConfidenceScore(relevantSegments);

            RegulationSearchResult result = new RegulationSearchResult(
                    answer,
                    references,
                    confidenceScore,
                    true
            );

            logger.info("Search completed with {} references, confidence: {}",
                    references.size(), confidenceScore);

            return result;

        } catch (Exception e) {
            logger.error("Error during regulation search", e);
            return createErrorResponse();
        }
    }

    /**
     * Hybrid Search 수행 (Vector + BM25 + RRF)
     */
    private List<EmbeddingMatch<TextSegment>> performHybridSearch(String query) {
        logger.info("Performing Hybrid Search (Vector + BM25 + RRF)");

        // Hybrid Search 수행
        int searchMaxResults = reRankingEnabled ? reRankingInitialResults : maxResults;
        HybridSearchResult hybridResult = hybridSearchService.search(query, searchMaxResults);

        logger.info("Hybrid Search completed: {} results (Vector: {}, BM25: {}, Fused: {}) in {}ms",
                hybridResult.getFusedResultCount(),
                hybridResult.getVectorResultCount(),
                hybridResult.getBm25ResultCount(),
                hybridResult.getFusedResultCount(),
                hybridResult.getSearchTimeMs());

        // ScoredSegment를 EmbeddingMatch로 변환 (embedding은 null - ReRanking에서 필요없음)
        List<EmbeddingMatch<TextSegment>> matches = hybridResult.getSegments().stream()
                .map(SearchResultConverter::toEmbeddingMatch)
                .collect(Collectors.toList());

        logger.info("Converted {} ScoredSegments to EmbeddingMatches", matches.size());

        // ReRanking 적용 (선택적)
        if (reRankingEnabled && scoringModel != null && !matches.isEmpty()) {
            logger.info("Applying ReRanking on Hybrid Search results ({} candidates)", matches.size());
            matches = performReRanking(query, matches);
            logger.info("ReRanking completed: {} results", matches.size());
        }

        return matches;
    }

    /**
     * Vector Search + ReRanking 수행
     */
    private List<EmbeddingMatch<TextSegment>> performVectorSearchWithReRanking(String query) {
        logger.info("Performing Vector Search with ReRanking");

        // Stage 1: 넓게 검색 (초기 후보)
        List<EmbeddingMatch<TextSegment>> relevantSegments = performVectorSearch(
                query,
                reRankingInitialResults,
                minScore  // 낮은 threshold로 넓게 검색
        );

        logger.info("Stage 1 (Vector Search): Retrieved {} candidates", relevantSegments.size());

        // Stage 2: ReRanking으로 정교하게 필터링
        if (!relevantSegments.isEmpty()) {
            relevantSegments = performReRanking(query, relevantSegments);
            logger.info("Stage 2 (ReRanking): Refined to {} results", relevantSegments.size());
        }

        return relevantSegments;
    }

    /**
     * 벡터 검색 수행 (기본 설정 사용)
     */
    private List<EmbeddingMatch<TextSegment>> performVectorSearch(String query) {
        return performVectorSearch(query, maxResults, minScore);
    }

    /**
     * 벡터 검색 수행 (파라미터 커스터마이징)
     */
    private List<EmbeddingMatch<TextSegment>> performVectorSearch(String query, int maxResults, double minScore) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding,
                maxResults
        );

        // 최소 점수 필터링
        matches = matches.stream()
                .filter(match -> match.score() >= minScore)
                .collect(Collectors.toList());

        logger.debug("Found {} relevant segments (maxResults={}, minScore={})", matches.size(), maxResults, minScore);
        return matches;
    }

    /**
     * ReRanking 수행 - Cohere Scoring Model을 사용하여 정교한 재정렬
     */
    private List<EmbeddingMatch<TextSegment>> performReRanking(
            String query,
            List<EmbeddingMatch<TextSegment>> candidates) {

        if (scoringModel == null || candidates.isEmpty()) {
            return candidates;
        }

        try {
            // 후보 세그먼트 추출
            List<TextSegment> segments = candidates.stream()
                    .map(EmbeddingMatch::embedded)
                    .collect(Collectors.toList());

            // Cohere Scoring Model로 재평가
            var scoringResponse = scoringModel.scoreAll(segments, query);
            List<Double> scores = scoringResponse.content();

            logger.info("Cohere ReRanking received {} scores for {} candidates", scores.size(), candidates.size());

            // 모든 점수 로그 (디버깅용)
            if (logger.isInfoEnabled() && !scores.isEmpty()) {
                String allScoresStr = scores.stream()
                        .map(s -> String.format("%.3f", s))
                        .collect(Collectors.joining(", "));
                logger.info("All ReRanking scores: [{}]", allScoresStr);
            }

            // 점수와 매칭 결과를 조합
            List<EmbeddingMatch<TextSegment>> reRankedResults = new ArrayList<>();
            int filteredCount = 0;

            for (int i = 0; i < candidates.size() && i < scores.size(); i++) {
                double reRankScore = scores.get(i);
                EmbeddingMatch<TextSegment> original = candidates.get(i);

                // 새로운 점수로 EmbeddingMatch 재생성
                EmbeddingMatch<TextSegment> reRanked = new EmbeddingMatch<>(
                        reRankScore,
                        original.embeddingId(),
                        original.embedding(),
                        original.embedded()
                );
                reRankedResults.add(reRanked);

                // 최소 점수 미만인 경우 카운트
                if (reRankScore < reRankingMinScore) {
                    filteredCount++;
                }
            }

            if (filteredCount > 0) {
                logger.warn("ReRanking: {} out of {} results have score < {} (threshold too high?)",
                        filteredCount, reRankedResults.size(), reRankingMinScore);
            }

            // 점수 기준 내림차순 정렬
            reRankedResults.sort((a, b) -> Double.compare(b.score(), a.score()));

            // 최종 결과 수로 제한 (점수 필터링 없이 상위 N개만 선택)
            int finalCount = Math.min(reRankingFinalResults, reRankedResults.size());
            if (reRankedResults.size() > finalCount) {
                reRankedResults = reRankedResults.subList(0, finalCount);
            }

            logger.info("ReRanking completed: {} candidates -> {} final results",
                    candidates.size(), reRankedResults.size());

            if (logger.isInfoEnabled() && !reRankedResults.isEmpty()) {
                String topScoresStr = reRankedResults.stream()
                        .map(r -> String.format("%.3f", r.score()))
                        .collect(Collectors.joining(", "));
                logger.info("Top ReRanked scores: [{}]", topScoresStr);
            }

            return reRankedResults;

        } catch (Exception e) {
            logger.error("Error during ReRanking, falling back to vector search results", e);
            return candidates.stream()
                    .limit(reRankingFinalResults)
                    .collect(Collectors.toList());
        }
    }

    /**
     * RAG 기반 답변 생성
     */
    private String generateAnswer(String question, List<EmbeddingMatch<TextSegment>> segments) {
        // 컨텍스트 구성
        String context = segments.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        // RAG 프롬프트 구성
        String prompt = String.format("""
            당신은 회사 규정 전문가입니다. 다음 규정 내용을 참고하여 사용자의 질문에 답변해주세요.

            [규정 내용]
            %s

            [질문]
            %s

            [답변 작성 지침]
            1. 제공된 규정 내용만을 기반으로 답변하세요
            2. 명확하고 간결하게 작성하세요
            3. 해당하는 규정 조항이 있다면 언급하세요
            4. 규정에 없는 내용은 추측하지 마세요
            5. 불확실한 경우 "해당 규정에서 명확히 언급되지 않았습니다"라고 답변하세요

            답변:
            """, context, question);

        String answer = chatModel.generate(prompt);
        return answer;
    }

    /**
     * 검색 결과를 RegulationReference 리스트로 변환
     */
    private List<RegulationReference> convertToReferences(
            List<EmbeddingMatch<TextSegment>> matches) {

        List<RegulationReference> references = new ArrayList<>();

        for (int i = 0; i < matches.size(); i++) {
            EmbeddingMatch<TextSegment> match = matches.get(i);
            TextSegment segment = match.embedded();

            String regulationType = segment.metadata().getString("regulation_type");
            String content = segment.text();
            double score = match.score();

            RegulationReference ref = new RegulationReference(
                    regulationType != null ? regulationType : "알 수 없음",
                    "N/A",  // 조항 번호는 별도 파싱 필요
                    content,
                    0,      // 페이지 번호는 메타데이터에서 가져와야 함
                    score
            );

            references.add(ref);
        }

        return references;
    }

    /**
     * 신뢰도 점수 계산
     */
    private double calculateConfidenceScore(List<EmbeddingMatch<TextSegment>> matches) {
        if (matches.isEmpty()) {
            return 0.0;
        }

        // 상위 매치들의 평균 점수
        double avgScore = matches.stream()
                .mapToDouble(EmbeddingMatch::score)
                .average()
                .orElse(0.0);

        return avgScore;
    }

    /**
     * Fallback 응답 생성 (검색 결과 없음)
     */
    private RegulationSearchResult createFallbackResponse() {
        return new RegulationSearchResult(
                "죄송합니다. 질문하신 내용에 대한 관련 규정을 찾을 수 없습니다. " +
                "인사팀 또는 관련 부서에 문의해주시기 바랍니다.",
                new ArrayList<>(),
                0.0,
                false
        );
    }

    /**
     * 에러 응답 생성
     */
    private RegulationSearchResult createErrorResponse() {
        return new RegulationSearchResult(
                "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                new ArrayList<>(),
                0.0,
                false
        );
    }

    /**
     * 임베딩 스토어에 저장된 세그먼트 수 반환
     */
    public int getIndexedSegmentsCount() {
        // InMemoryEmbeddingStore는 size 메서드가 없으므로 별도 카운터 필요
        return 0; // TODO: 실제 카운트 구현
    }

    /**
     * 현재 임베딩 스토어 반환 (영속성을 위해)
     */
    public InMemoryEmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }

    /**
     * 임베딩 스토어 설정 (로드시 사용)
     */
    public void setEmbeddingStore(InMemoryEmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingStore = embeddingStore;
        logger.info("Embedding store has been updated");
    }
}
