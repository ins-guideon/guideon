package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationReference;
import com.guideon.model.RegulationSearchResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
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
    private final EmbeddingStore<TextSegment> embeddingStore;

    private final int maxResults;
    private final double minScore;
    private final int chunkSize;
    private final int chunkOverlap;

    /**
     * application.properties 기반 생성자
     */
    public RegulationSearchService(ConfigLoader config) {
        String apiKey = config.getGeminiApiKey();

        // Gemini Chat Model 초기화
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.2)
                .build();

        // Embedding Model 초기화 (로컬 모델 사용)
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // In-Memory Embedding Store (실제 운영시 Qdrant로 교체)
        this.embeddingStore = new InMemoryEmbeddingStore<>();

        // Properties에서 설정값 로드
        this.maxResults = config.getVectorSearchMaxResults();
        this.minScore = config.getVectorSearchMinScore();
        this.chunkSize = config.getRagChunkSize();
        this.chunkOverlap = config.getRagChunkOverlap();

        logger.info("RegulationSearchService initialized with maxResults={}, minScore={}, chunkSize={}, chunkOverlap={}",
                maxResults, minScore, chunkSize, chunkOverlap);
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

        // Embedding Model 초기화 (로컬 모델 사용)
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // In-Memory Embedding Store (실제 운영시 Qdrant로 교체)
        this.embeddingStore = new InMemoryEmbeddingStore<>();

        // 기본값 사용
        this.maxResults = 5;
        this.minScore = 0.5;
        this.chunkSize = 500;
        this.chunkOverlap = 100;

        logger.info("RegulationSearchService initialized with default values");
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

        // 임베딩 생성 및 저장
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }

        logger.info("Indexed {} segments for {}", segments.size(), regulationType);
    }

    /**
     * 분석된 쿼리를 기반으로 규정 검색 및 답변 생성
     */
    public RegulationSearchResult search(QueryAnalysisResult analysis) {
        logger.info("Searching regulations for query: {}", analysis.getOriginalQuery());

        try {
            // 1. 벡터 검색 수행
            List<EmbeddingMatch<TextSegment>> relevantSegments = performVectorSearch(
                    analysis.getSearchQuery()
            );

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
     * 벡터 검색 수행
     */
    private List<EmbeddingMatch<TextSegment>> performVectorSearch(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding,
                maxResults
        );

        // 최소 점수 필터링
        matches = matches.stream()
                .filter(match -> match.score() >= minScore)
                .collect(Collectors.toList());

        logger.debug("Found {} relevant segments", matches.size());
        return matches;
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
}
