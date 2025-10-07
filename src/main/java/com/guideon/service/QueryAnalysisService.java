package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 자연어 질문을 분석하여 검색 가능한 형태로 변환하는 서비스
 */
public class QueryAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(QueryAnalysisService.class);

    private final ChatLanguageModel chatModel;
    private final List<String> regulationTypes;

    /**
     * application.properties 기반 생성자
     */
    public QueryAnalysisService(ConfigLoader config) {
        String apiKey = config.getGeminiApiKey();

        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.3) // 일관된 분석을 위해 낮은 temperature
                .build();

        // Properties 파일에서 규정 유형 로드
        String typesStr = config.getProperty("regulation.types", "");
        if (typesStr.isEmpty()) {
            this.regulationTypes = getDefaultRegulationTypes();
        } else {
            this.regulationTypes = Arrays.asList(typesStr.split(","));
        }

        logger.info("QueryAnalysisService initialized with {} regulation types", regulationTypes.size());
    }

    /**
     * API 키 직접 전달 생성자 (하위 호환성)
     */
    @Deprecated
    public QueryAnalysisService(String apiKey) {
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.3)
                .build();
        this.regulationTypes = getDefaultRegulationTypes();
    }

    // 사규 유형 목록 (CLUADE.md 기반)
    private static List<String> getDefaultRegulationTypes() {
        return Arrays.asList(
            "이사회규정", "접대비사용규정", "윤리규정", "출장여비지급규정",
            "주식매수선택권운영규정", "노사협의회규정", "취업규칙", "매출채권관리규정",
            "금융자산 운용규정", "문서관리규정", "재고관리규정", "계약검토규정",
            "사규관리규정", "임원퇴직금지급규정", "임원보수규정", "주주총회운영규정",
            "경비지급규정", "복리후생비규정", "보안관리규정", "위임전결규정",
            "우리사주운영규정", "내부정보관리규정", "회계관리규정", "특수관계자 거래규정",
            "조직 및 업무분장규정", "자금관리규정", "인장관리규정"
        );
    }

    /**
     * 자연어 질문을 분석하여 구조화된 검색 쿼리로 변환
     */
    public QueryAnalysisResult analyzeQuery(String userQuery) {
        logger.info("Analyzing query: {}", userQuery);

        try {
            // AI를 통한 질문 분석
            String analysisPrompt = buildAnalysisPrompt(userQuery);
            String aiResponse = chatModel.generate(analysisPrompt);

            // 분석 결과 파싱
            QueryAnalysisResult result = parseAnalysisResponse(userQuery, aiResponse);

            logger.info("Query analysis completed: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("Error analyzing query", e);
            // Fallback: 간단한 키워드 추출
            return createFallbackAnalysis(userQuery);
        }
    }

    /**
     * AI 분석을 위한 프롬프트 생성
     */
    private String buildAnalysisPrompt(String userQuery) {
        return String.format("""
            당신은 회사 규정 검색 시스템의 질문 분석 전문가입니다.
            사용자의 자연어 질문을 분석하여 다음 정보를 추출해주세요:

            사용자 질문: "%s"

            다음 형식으로 답변해주세요:
            KEYWORDS: [쉼표로 구분된 핵심 키워드 목록]
            REGULATION_TYPES: [관련될 것으로 예상되는 규정 유형, 다음 목록에서 선택]
            %s
            INTENT: [질문 의도: 정보조회/절차안내/기준확인/자격요건/예외사항 중 하나]
            SEARCH_QUERY: [검색에 최적화된 쿼리문]

            규정 유형은 위 목록에서만 선택하고, 관련 없으면 "일반"으로 표시하세요.
            """, userQuery, String.join(", ", regulationTypes));
    }

    /**
     * AI 응답을 파싱하여 QueryAnalysisResult 객체 생성
     */
    private QueryAnalysisResult parseAnalysisResponse(String originalQuery, String aiResponse) {
        QueryAnalysisResult result = new QueryAnalysisResult();
        result.setOriginalQuery(originalQuery);

        // KEYWORDS 추출
        List<String> keywords = extractField(aiResponse, "KEYWORDS");
        result.setKeywords(keywords);

        // REGULATION_TYPES 추출
        List<String> regulationTypes = extractField(aiResponse, "REGULATION_TYPES");
        result.setRegulationTypes(regulationTypes);

        // INTENT 추출
        String intent = extractSingleField(aiResponse, "INTENT");
        result.setIntent(intent);

        // SEARCH_QUERY 추출
        String searchQuery = extractSingleField(aiResponse, "SEARCH_QUERY");
        result.setSearchQuery(searchQuery);

        return result;
    }

    /**
     * 응답에서 특정 필드 추출 (리스트 형태)
     */
    private List<String> extractField(String response, String fieldName) {
        List<String> values = new ArrayList<>();
        Pattern pattern = Pattern.compile(fieldName + ":\\s*(.+?)(?=\\n[A-Z_]+:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            String value = matcher.group(1).trim();
            // 대괄호 제거 및 쉼표로 분리
            value = value.replaceAll("[\\[\\]]", "");
            String[] items = value.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        }

        return values;
    }

    /**
     * 응답에서 단일 필드 추출
     */
    private String extractSingleField(String response, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*(.+?)(?=\\n[A-Z_]+:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "";
    }

    /**
     * AI 분석 실패 시 폴백 분석
     */
    private QueryAnalysisResult createFallbackAnalysis(String userQuery) {
        logger.warn("Using fallback analysis for query: {}", userQuery);

        QueryAnalysisResult result = new QueryAnalysisResult();
        result.setOriginalQuery(userQuery);

        // 간단한 키워드 추출 (공백 기준)
        List<String> keywords = Arrays.asList(userQuery.split("\\s+"));
        result.setKeywords(keywords);

        // 규정 유형 매칭
        List<String> matchedTypes = new ArrayList<>();
        for (String regType : regulationTypes) {
            if (userQuery.contains(regType) || userQuery.contains(regType.replace("규정", ""))) {
                matchedTypes.add(regType);
            }
        }
        result.setRegulationTypes(matchedTypes.isEmpty() ? List.of("일반") : matchedTypes);

        result.setIntent("정보조회");
        result.setSearchQuery(userQuery);

        return result;
    }
}
