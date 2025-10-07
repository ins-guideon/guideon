package com.guideon;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationSearchResult;
import com.guideon.service.QueryAnalysisService;
import com.guideon.service.RegulationSearchService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 규정 Q&A 시스템 메인 클래스
 * CLUADE.md 아키텍처 기반 (Java + LangChain4j + Gemini)
 */
public class RegulationQASystem {
    private static final Logger logger = LoggerFactory.getLogger(RegulationQASystem.class);

    private final QueryAnalysisService queryAnalysisService;
    private final RegulationSearchService regulationSearchService;
    private final ConfigLoader config;

    /**
     * application.properties 기반 생성자
     */
    public RegulationQASystem() {
        this.config = new ConfigLoader();
        this.queryAnalysisService = new QueryAnalysisService(config);
        this.regulationSearchService = new RegulationSearchService(config);

        logger.info("RegulationQASystem initialized from application.properties");
    }

    /**
     * 커스텀 설정 파일 경로를 사용하는 생성자
     */
    public RegulationQASystem(String configFilePath) {
        this.config = new ConfigLoader(configFilePath);
        this.queryAnalysisService = new QueryAnalysisService(config);
        this.regulationSearchService = new RegulationSearchService(config);

        logger.info("RegulationQASystem initialized from: {}", configFilePath);
    }

    /**
     * ConfigLoader를 직접 전달받는 생성자
     */
    public RegulationQASystem(ConfigLoader config) {
        this.config = config;
        this.queryAnalysisService = new QueryAnalysisService(config);
        this.regulationSearchService = new RegulationSearchService(config);

        logger.info("RegulationQASystem initialized with provided ConfigLoader");
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

    /**
     * 메인 메서드 - 사용 예시
     */
    public static void main(String[] args) {
        try {
            // 시스템 초기화 - application.properties에서 설정 로드
            RegulationQASystem system;

            if (args.length > 0) {
                // 커맨드 라인 인자로 설정 파일 경로가 제공된 경우
                String configPath = args[0];
                logger.info("Using configuration file: {}", configPath);
                system = new RegulationQASystem(configPath);
            } else {
                // 기본 application.properties 사용
                logger.info("Using default application.properties");
                system = new RegulationQASystem();
            }

            // 사용 예시 1: 규정 문서 업로드
            // system.uploadRegulationDocument("path/to/취업규칙.txt", "취업규칙");

            // 사용 예시 2: 질의응답
            String question = "연차 휴가는 몇 일인가요?";
            RegulationSearchResult result = system.askQuestion(question);

            // 결과 출력
            System.out.println("\n=== 질문 ===");
            System.out.println(question);

            System.out.println("\n=== 답변 ===");
            System.out.println(result.getAnswer());

            System.out.println("\n=== 근거 조항 ===");
            if (result.getReferences().isEmpty()) {
                System.out.println("관련 규정을 찾을 수 없습니다.");
            } else {
                result.getReferences().forEach(ref -> {
                    System.out.printf("- %s (관련도: %.2f)\n",
                            ref.getDocumentName(),
                            ref.getRelevanceScore());
                    System.out.printf("  내용: %s\n\n",
                            ref.getContent().substring(0, Math.min(100, ref.getContent().length())) + "...");
                });
            }

            System.out.printf("\n신뢰도: %.2f%%\n", result.getConfidenceScore() * 100);

        } catch (IllegalStateException e) {
            // API 키 미설정 등의 설정 오류
            logger.error("Configuration error: {}", e.getMessage());
            System.err.println("\nError: " + e.getMessage());
            System.err.println("\n설정 방법:");
            System.err.println("1. application.properties 파일에 gemini.api.key 설정");
            System.err.println("2. 환경변수 GOOGLE_API_KEY 설정");
            System.err.println("3. 커맨드 라인으로 설정 파일 지정: java -jar app.jar custom.properties");
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error occurred", e);
            System.err.println("\nUnexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
