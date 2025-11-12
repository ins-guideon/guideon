package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.HybridSearchResult;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationReference;
import com.guideon.model.RegulationSearchResult;
import com.guideon.model.ScoredSegment;
import com.guideon.util.EnhancedContextBuilder;
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

        logger.info(
                "RegulationSearchService initialized with maxResults={}, minScore={}, chunkSize={}, chunkOverlap={}, reRankingEnabled={}, hybridSearchEnabled={}",
                maxResults, minScore, chunkSize, chunkOverlap, reRankingEnabled && scoringModel != null,
                hybridSearchEnabled);
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

        logger.info(
                "RegulationSearchService initialized with default values (ReRanking disabled, Hybrid Search disabled)");
    }

    /**
     * 규정 문서 인덱싱
     */
    public void indexDocument(Document document, String regulationType) {
        logger.info("Indexing document for regulation type: {}", regulationType);

        // 문서를 청크로 분할
        DocumentSplitter splitter = DocumentSplitters.recursive(
                chunkSize, // maxSegmentSize
                chunkOverlap // maxOverlapSize
        );

        List<TextSegment> segments = splitter.split(document);

        // 각 세그먼트에 메타데이터 추가
        for (TextSegment segment : segments) {
            segment.metadata().put("regulation_type", regulationType);
            String fileName = document.metadata().getString("file_name");
            if (fileName != null) {
                segment.metadata().put("file_name", fileName);
            }
            String documentId = document.metadata().getString("document_id");
            if (documentId != null) {
                segment.metadata().put("document_id", documentId);
            }
        }

        // 임베딩 생성 및 Vector Store에 저장
        for (int segmentIndex = 0; segmentIndex < segments.size(); segmentIndex++) {
            TextSegment segment = segments.get(segmentIndex);
            Embedding embedding = embeddingModel.embed(segment).content();
            String embeddingId = embeddingStore.add(embedding, segment);

            // Hybrid Search가 활성화된 경우 BM25 인덱스에도 추가
            if (hybridSearchEnabled && hybridSearchService != null) {
                String segmentId = embeddingId != null ? embeddingId
                        : String.format("%s_segment_%d", regulationType, segmentIndex);
                hybridSearchService.indexSegment(segment, regulationType, segmentId);
            }
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
                        minScore);
                logger.info("Vector Search: Found {} results", relevantSegments.size());
            }

            // 2. 검색 결과가 없으면 Fallback 응답
            if (relevantSegments.isEmpty()) {
                return createFallbackResponse();
            }

            // 3. 검색된 세그먼트를 참조 정보로 변환
            List<RegulationReference> references = convertToReferences(relevantSegments);

            // 4. RAG 기반 답변 생성 (구조화된 컨텍스트 사용)
            String answer = generateAnswer(
                    analysis.getOriginalQuery(),
                    relevantSegments,
                    analysis);

            // 5. 신뢰도 점수 계산
            double confidenceScore = calculateConfidenceScore(relevantSegments);

            RegulationSearchResult result = new RegulationSearchResult(
                    answer,
                    references,
                    confidenceScore,
                    true);

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
                minScore // 낮은 threshold로 넓게 검색
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
                maxResults);

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
                        original.embedded());
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
     * RAG 기반 답변 생성 (구조화된 컨텍스트 사용 + 품질 개선)
     */
    private String generateAnswer(
            String question,
            List<EmbeddingMatch<TextSegment>> segments,
            QueryAnalysisResult analysis) {

        logger.info("========================================");
        logger.info("Generating Answer with Quality Enhancement");
        logger.info("========================================");

        // 1. 구조화된 컨텍스트 생성
        String context = EnhancedContextBuilder.buildStructuredContext(segments, analysis);
        logger.debug("Generated structured context (length: {} chars)", context.length());

        // 2. 의도별 최적화된 프롬프트 생성
        String prompt = com.guideon.util.PromptTemplate.buildPrompt(question, context, analysis);
        logger.debug("Prompt built with intent-specific guidelines");

        // 3. LLM으로 답변 생성
        logger.debug("Generating answer with LLM...");
        String rawAnswer = chatModel.generate(prompt);
        logger.info("Raw answer generated (length: {} chars)", rawAnswer.length());

        // 4. 답변 검증
        boolean isValid = com.guideon.util.AnswerQualityEnhancer.validateAnswer(rawAnswer);
        if (!isValid) {
            logger.warn("Answer validation failed, but continuing with quality enhancement");
        }

        // 5. 답변 품질 점수 계산
        double qualityScore = com.guideon.util.AnswerQualityEnhancer.calculateAnswerQualityScore(rawAnswer, analysis);
        logger.info("Answer quality score: {:.3f}", qualityScore);

        // 6. 참조 조항 추출
        List<String> referencedArticles = com.guideon.util.AnswerQualityEnhancer.extractReferencedArticles(rawAnswer);
        logger.info("Extracted {} article references from answer", referencedArticles.size());

        // 7. 답변 개선 (후처리 및 포맷팅)
        String enhancedAnswer = com.guideon.util.AnswerQualityEnhancer.enhanceAnswer(rawAnswer, analysis,
                referencedArticles);
        logger.info("Answer enhanced (final length: {} chars)", enhancedAnswer.length());

        // 8. 신뢰도 기반 안내 메시지 추가
        double confidenceScore = calculateConfidenceScore(segments);
        String finalAnswer = com.guideon.util.AnswerQualityEnhancer.addConfidenceIndicator(enhancedAnswer,
                confidenceScore);

        logger.info("========================================");
        logger.info("Answer Generation Complete");
        logger.info("  Quality Score: {:.3f}", qualityScore);
        logger.info("  Confidence Score: {:.3f}", confidenceScore);
        logger.info("  Article References: {}", referencedArticles.size());
        logger.info("  Final Length: {} chars", finalAnswer.length());
        logger.info("========================================");

        return finalAnswer;
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
                    "N/A", // 조항 번호는 별도 파싱 필요
                    content,
                    0, // 페이지 번호는 메타데이터에서 가져와야 함
                    score);

            references.add(ref);
        }

        return references;
    }

    /**
     * 신뢰도 점수 계산 (개선된 알고리즘)
     *
     * 다양한 요소를 고려하여 신뢰도를 계산:
     * 1. 검색 방식별 점수 정규화 (ReRanking, RRF, Vector Search)
     * 2. 상위 결과 가중치 적용
     * 3. 점수 분포 일관성 평가
     * 4. 결과 개수 기반 신뢰도 조정
     */
    private double calculateConfidenceScore(List<EmbeddingMatch<TextSegment>> matches) {
        if (matches.isEmpty()) {
            return 0.0;
        }

        // 검색 방식 감지 및 점수 정규화
        double normalizedScore;

        if (reRankingEnabled && scoringModel != null) {
            // Case 1: ReRanking 점수 (0.0~1.0, 실제로는 0.001~0.3)
            // Cohere ReRanking은 매우 낮은 점수를 반환하므로 정규화 필요
            normalizedScore = normalizeReRankingScores(matches);
            logger.debug("Confidence calculation: ReRanking mode, normalized score={}", normalizedScore);

        } else if (hybridSearchEnabled && hybridSearchService != null) {
            // Case 2: Hybrid Search (RRF 점수, 0.01~0.03)
            // RRF 점수는 매우 낮으므로 순위 기반으로 재계산
            normalizedScore = normalizeRRFScores(matches);
            logger.debug("Confidence calculation: Hybrid Search mode, normalized score={}", normalizedScore);

        } else {
            // Case 3: 순수 Vector Search (코사인 유사도, 0.5~0.99)
            normalizedScore = normalizeVectorSearchScores(matches);
            logger.debug("Confidence calculation: Vector Search mode, normalized score={}", normalizedScore);
        }

        // 결과 개수 기반 신뢰도 조정 (너무 적으면 패널티)
        double countFactor = calculateCountFactor(matches.size());

        // 점수 분포 일관성 평가 (점수가 고르면 더 신뢰)
        double consistencyFactor = calculateConsistencyFactor(matches);

        // 최종 신뢰도 = 정규화된 점수 × 개수 팩터 × 일관성 팩터
        double finalConfidence = normalizedScore * countFactor * consistencyFactor;

        // 0.0~1.0 범위로 제한
        finalConfidence = Math.max(0.0, Math.min(1.0, finalConfidence));

        logger.info("Confidence score calculated: {} (normalized={}, count_factor={}, consistency_factor={})",
                String.format("%.3f", finalConfidence),
                String.format("%.3f", normalizedScore),
                String.format("%.3f", countFactor),
                String.format("%.3f", consistencyFactor));

        return finalConfidence;
    }

    /**
     * ReRanking 점수 정규화
     * Cohere는 0.001~0.3 범위의 낮은 점수를 반환하므로 정규화 필요
     */
    private double normalizeReRankingScores(List<EmbeddingMatch<TextSegment>> matches) {
        // 상위 3개 결과의 가중 평균 사용
        double weightedSum = 0.0;
        double weightSum = 0.0;

        int topK = Math.min(3, matches.size());
        for (int i = 0; i < topK; i++) {
            double weight = 1.0 / (i + 1); // 1.0, 0.5, 0.33...
            double score = matches.get(i).score();

            // ReRanking 점수를 0~1 범위로 매핑 (더 관대하게 조정)
            // Cohere ReRanking은 절대값이 낮지만 상대적 순위가 중요함
            double normalizedScore;
            if (score >= 0.2) {
                normalizedScore = 0.85 + (score - 0.2) * 0.75; // 0.85~1.0 (0.2+ 매우 높음)
            } else if (score >= 0.08) {
                normalizedScore = 0.70 + (score - 0.08) * 1.25; // 0.70~0.85 (0.08~0.2 높음)
            } else if (score >= 0.03) {
                normalizedScore = 0.55 + (score - 0.03) * 3.0; // 0.55~0.70 (0.03~0.08 중간)
            } else if (score >= 0.01) {
                normalizedScore = 0.40 + (score - 0.01) * 7.5; // 0.40~0.55 (0.01~0.03 낮음)
            } else {
                normalizedScore = Math.max(0.25, score * 40.0); // 0.25~0.40 (최소 0.25 보장)
            }

            weightedSum += normalizedScore * weight;
            weightSum += weight;
        }

        return weightSum > 0 ? weightedSum / weightSum : 0.0;
    }

    /**
     * RRF 점수 정규화 (Hybrid Search)
     * RRF 점수는 순위 기반이므로 점수 자체보다 순위를 활용
     */
    private double normalizeRRFScores(List<EmbeddingMatch<TextSegment>> matches) {
        // RRF 점수는 매우 낮으므로 (0.01~0.03), 순위 기반으로 신뢰도 계산
        if (matches.isEmpty())
            return 0.0;

        // 최상위 결과의 점수가 높으면 신뢰도 높음
        double topScore = matches.get(0).score();

        // RRF 점수 범위: 0.016 (1위) ~ 0.003 (마지막)
        // Phase 4.2: BM25 가중치가 높아져서 RRF 신뢰도를 더 관대하게 평가
        if (topScore >= 0.013) {
            return 0.85 + Math.min(0.15, (topScore - 0.013) * 20.0); // 0.85~1.0 (최고)
        } else if (topScore >= 0.008) {
            return 0.70 + (topScore - 0.008) * 30.0; // 0.70~0.85 (높음)
        } else if (topScore >= 0.004) {
            return 0.55 + (topScore - 0.004) * 37.5; // 0.55~0.70 (중간)
        } else {
            return Math.max(0.40, 0.35 + topScore * 50.0); // 0.40~0.55 (최소 0.40 보장)
        }
    }

    /**
     * Vector Search 점수 정규화 (코사인 유사도)
     */
    private double normalizeVectorSearchScores(List<EmbeddingMatch<TextSegment>> matches) {
        // 상위 3개 결과의 가중 평균
        double weightedSum = 0.0;
        double weightSum = 0.0;

        int topK = Math.min(3, matches.size());
        for (int i = 0; i < topK; i++) {
            double weight = 1.0 / (i + 1); // 1.0, 0.5, 0.33...
            double score = matches.get(i).score();

            // 코사인 유사도: 0.8+ = 높음, 0.7~0.8 = 중간, 0.6~0.7 = 낮음, 0.6 미만 = 매우 낮음
            // Phase 4.2: Hybrid Search에서 BM25 가중치가 높으므로 Vector는 보조 역할
            // 따라서 더 관대하게 평가
            double normalizedScore;
            if (score >= 0.8) {
                normalizedScore = 0.75 + (score - 0.8) * 1.25; // 0.75~1.0
            } else if (score >= 0.7) {
                normalizedScore = 0.60 + (score - 0.7) * 1.5; // 0.60~0.75
            } else if (score >= 0.6) {
                normalizedScore = 0.45 + (score - 0.6) * 1.5; // 0.45~0.60
            } else if (score >= 0.5) {
                normalizedScore = 0.30 + (score - 0.5) * 1.5; // 0.30~0.45
            } else {
                normalizedScore = Math.max(0.15, score * 0.6); // 0.15~0.30 (최소 0.15 보장)
            }

            weightedSum += normalizedScore * weight;
            weightSum += weight;
        }

        return weightSum > 0 ? weightedSum / weightSum : 0.0;
    }

    /**
     * 결과 개수 기반 신뢰도 조정
     * 결과가 너무 적으면 신뢰도 감소
     */
    private double calculateCountFactor(int resultCount) {
        if (resultCount >= 3) {
            return 1.0; // 충분한 결과
        } else if (resultCount == 2) {
            return 0.9; // 약간 부족
        } else if (resultCount == 1) {
            return 0.75; // 단일 결과 (신뢰도 낮춤)
        } else {
            return 0.0; // 결과 없음
        }
    }

    /**
     * 점수 분포 일관성 평가
     * 상위 결과들의 점수가 비슷하면 더 신뢰할 수 있음
     */
    private double calculateConsistencyFactor(List<EmbeddingMatch<TextSegment>> matches) {
        if (matches.size() < 2) {
            return 1.0; // 단일 결과는 일관성 평가 불가
        }

        // 상위 3개 결과의 점수 표준편차 계산
        int topK = Math.min(3, matches.size());
        double[] scores = new double[topK];
        double sum = 0.0;

        for (int i = 0; i < topK; i++) {
            scores[i] = matches.get(i).score();
            sum += scores[i];
        }

        double mean = sum / topK;
        double variance = 0.0;

        for (double score : scores) {
            variance += Math.pow(score - mean, 2);
        }

        double stdDev = Math.sqrt(variance / topK);

        // 표준편차가 낮을수록 일관성 높음
        // 0~0.05: 매우 일관적 (1.0), 0.05~0.1: 일관적 (0.95), 0.1+: 불일치 (0.85)
        if (stdDev < 0.05) {
            return 1.0;
        } else if (stdDev < 0.1) {
            return 0.95;
        } else {
            return 0.85;
        }
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
                false);
    }

    /**
     * 에러 응답 생성
     */
    private RegulationSearchResult createErrorResponse() {
        return new RegulationSearchResult(
                "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                new ArrayList<>(),
                0.0,
                false);
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
