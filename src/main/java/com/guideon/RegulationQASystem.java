package com.guideon;

import com.guideon.config.GuideonProperties;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationSearchResult;
import com.guideon.service.QueryAnalysisService;
import com.guideon.service.RegulationSearchService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 규정 Q&A 시스템 메인 클래스
 * Spring Bean으로 등록되어 자동 설정된 서비스들을 사용합니다.
 */
@Component
public class RegulationQASystem {
    private static final Logger logger = LoggerFactory.getLogger(RegulationQASystem.class);

    private final QueryAnalysisService queryAnalysisService;
    private final RegulationSearchService regulationSearchService;
    private final GuideonProperties properties;

    /**
     * Spring 의존성 주입을 위한 생성자
     */
    public RegulationQASystem(
            QueryAnalysisService queryAnalysisService,
            RegulationSearchService regulationSearchService,
            GuideonProperties properties) {
        this.queryAnalysisService = queryAnalysisService;
        this.regulationSearchService = regulationSearchService;
        this.properties = properties;
        logger.info("RegulationQASystem initialized as Spring Component");
    }

    /**
     * 자연어 질문을 받아 AI 분석 후 규정 검색 결과 반환
     *
     * @param userQuestion 사용자의 자연어 질문
     * @return 규정 검색 결과 (답변 + 근거 조항)
     */
    public RegulationSearchResult askQuestion(String userQuestion) {
        logger.info("Processing question: {}", userQuestion);

        // 1단계: 자연어 질문 분석
        QueryAnalysisResult analysis = queryAnalysisService.analyzeQuery(userQuestion);

        logger.info("Query analyzed - Keywords: {}, Regulation Types: {}, Intent: {}",
                analysis.getKeywords(),
                analysis.getRegulationTypes(),
                analysis.getIntent());

        // 2단계: 분석 결과를 기반으로 규정 검색 및 답변 생성
        RegulationSearchResult searchResult = regulationSearchService.search(analysis);

        logger.info("Search completed - Found regulation: {}, Confidence: {}",
                searchResult.isFoundRelevantRegulation(),
                searchResult.getConfidenceScore());

        return searchResult;
    }

    /**
     * 규정 문서 업로드 및 인덱싱 (파일 경로)
     *
     * @param filePath 규정 문서 파일 경로
     * @param regulationType 규정 유형 (예: "취업규칙", "경비지급규정")
     */
    public void uploadRegulationDocument(String filePath, String regulationType) {
        logger.info("Uploading regulation document: {} (Type: {})", filePath, regulationType);

        try {
            Path path = Paths.get(filePath);

            // 문서 로드 (텍스트 파일 기준, PDF/Word는 별도 파서 필요)
            Document document = FileSystemDocumentLoader.loadDocument(
                    path,
                    new TextDocumentParser()
            );

            // 문서 인덱싱
            regulationSearchService.indexDocument(document, regulationType);

            logger.info("Document indexed successfully: {}", filePath);

        } catch (Exception e) {
            logger.error("Failed to upload regulation document: {}", filePath, e);
            throw new RuntimeException("문서 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 규정 문서 업로드 및 인덱싱 (Document 객체)
     * 테스트나 프로그래밍 방식으로 문서를 직접 전달할 때 사용
     *
     * @param document Document 객체
     * @param regulationType 규정 유형 (예: "취업규칙", "경비지급규정")
     */
    public void uploadRegulationDocument(Document document, String regulationType) {
        logger.info("Uploading regulation document (Type: {})", regulationType);

        try {
            // 문서 인덱싱
            regulationSearchService.indexDocument(document, regulationType);

            logger.info("Document indexed successfully");

        } catch (Exception e) {
            logger.error("Failed to upload regulation document", e);
            throw new RuntimeException("문서 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 시스템 상태 확인
     */
    public String getSystemStatus() {
        return String.format(
                "Regulation Q&A System Status:\n" +
                "- Query Analysis: Active\n" +
                "- Search Service: Active\n" +
                "- Indexed Segments: %d",
                regulationSearchService.getIndexedSegmentsCount()
        );
    }
}
