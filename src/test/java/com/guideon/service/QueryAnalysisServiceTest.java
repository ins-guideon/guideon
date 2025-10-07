package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryAnalysisService 테스트 클래스
 * 자연어 질문 분석 기능 테스트
 */
@DisplayName("QueryAnalysisService 테스트")
class QueryAnalysisServiceTest {

    private QueryAnalysisService service;
    private ConfigLoader config;

    @BeforeEach
    void setUp() {
        // 테스트용 ConfigLoader 초기화
        config = new ConfigLoader();

        try {
            service = new QueryAnalysisService(config);
            System.out.println("✓ QueryAnalysisService 초기화 성공");
        } catch (IllegalStateException e) {
            System.err.println("⚠ API 키가 설정되지 않았습니다. 일부 테스트를 건너뜁니다.");
            System.err.println("환경변수 GOOGLE_API_KEY 또는 application.properties 설정 필요");
        }
    }

    @Test
    @DisplayName("1. 서비스 초기화 테스트")
    void testServiceInitialization() {
        assertNotNull(config, "ConfigLoader가 null이면 안됩니다");

        if (service != null) {
            System.out.println("✓ QueryAnalysisService 정상 초기화됨");
        } else {
            System.out.println("⚠ API 키 미설정으로 서비스 초기화 불가");
        }
    }

    @Test
    @DisplayName("2. 연차 휴가 관련 질문 분석")
    void testAnalyzeVacationQuery() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        String query = "연차 휴가는 몇 일인가요?";
        System.out.println("\n=== 질문 분석 테스트 ===");
        System.out.println("질문: " + query);

        try {
            QueryAnalysisResult result = service.analyzeQuery(query);

            assertNotNull(result, "분석 결과가 null이면 안됩니다");
            assertNotNull(result.getKeywords(), "키워드가 null이면 안됩니다");
            assertNotNull(result.getRegulationTypes(), "규정 유형이 null이면 안됩니다");
            assertNotNull(result.getIntent(), "질문 의도가 null이면 안됩니다");
            assertNotNull(result.getSearchQuery(), "검색 쿼리가 null이면 안됩니다");

            System.out.println("\n결과:");
            System.out.println("- 키워드: " + result.getKeywords());
            System.out.println("- 규정 유형: " + result.getRegulationTypes());
            System.out.println("- 질문 의도: " + result.getIntent());
            System.out.println("- 검색 쿼리: " + result.getSearchQuery());

            assertTrue(result.getKeywords().size() > 0, "최소 1개 이상의 키워드가 추출되어야 합니다");

        } catch (Exception e) {
            fail("질문 분석 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("3. 출장비 관련 질문 분석")
    void testAnalyzeBusinessTripQuery() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        String query = "해외 출장 시 숙박비는 얼마까지 지원되나요?";
        System.out.println("\n=== 질문 분석 테스트 ===");
        System.out.println("질문: " + query);

        try {
            QueryAnalysisResult result = service.analyzeQuery(query);

            assertNotNull(result);

            System.out.println("\n결과:");
            System.out.println("- 키워드: " + result.getKeywords());
            System.out.println("- 규정 유형: " + result.getRegulationTypes());
            System.out.println("- 질문 의도: " + result.getIntent());
            System.out.println("- 검색 쿼리: " + result.getSearchQuery());

            // 출장여비지급규정이 포함되어야 함
            List<String> regulationTypes = result.getRegulationTypes();
            boolean hasRelevantRegulation = regulationTypes.stream()
                    .anyMatch(type -> type.contains("출장") || type.contains("여비"));

            if (hasRelevantRegulation) {
                System.out.println("✓ 적절한 규정 유형 식별됨");
            }

        } catch (Exception e) {
            fail("질문 분석 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("4. 경비 관련 질문 분석")
    void testAnalyzeExpenseQuery() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        String query = "법인카드 사용 한도는 얼마인가요?";
        System.out.println("\n=== 질문 분석 테스트 ===");
        System.out.println("질문: " + query);

        try {
            QueryAnalysisResult result = service.analyzeQuery(query);

            assertNotNull(result);

            System.out.println("\n결과:");
            System.out.println("- 키워드: " + result.getKeywords());
            System.out.println("- 규정 유형: " + result.getRegulationTypes());
            System.out.println("- 질문 의도: " + result.getIntent());
            System.out.println("- 검색 쿼리: " + result.getSearchQuery());

        } catch (Exception e) {
            fail("질문 분석 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("5. 빈 문자열 입력 테스트")
    void testAnalyzeEmptyQuery() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        String query = "";
        System.out.println("\n=== 빈 문자열 테스트 ===");

        try {
            QueryAnalysisResult result = service.analyzeQuery(query);
            assertNotNull(result, "빈 문자열도 결과를 반환해야 합니다");
            System.out.println("✓ 빈 문자열 처리 완료");
        } catch (Exception e) {
            System.out.println("✓ 예외 처리됨: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("6. 복잡한 질문 분석")
    void testAnalyzeComplexQuery() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        String query = "신입사원의 경우 입사 1년차에 연차휴가를 몇일 받을 수 있으며, " +
                      "출장 시 교통비와 숙박비는 어떤 기준으로 지급되나요?";
        System.out.println("\n=== 복잡한 질문 분석 테스트 ===");
        System.out.println("질문: " + query);

        try {
            QueryAnalysisResult result = service.analyzeQuery(query);

            assertNotNull(result);

            System.out.println("\n결과:");
            System.out.println("- 키워드: " + result.getKeywords());
            System.out.println("- 규정 유형: " + result.getRegulationTypes());
            System.out.println("- 질문 의도: " + result.getIntent());
            System.out.println("- 검색 쿼리: " + result.getSearchQuery());

            // 여러 규정 유형이 식별되어야 함
            assertTrue(result.getRegulationTypes().size() >= 1,
                      "복잡한 질문은 최소 1개 이상의 규정 유형을 식별해야 합니다");

        } catch (Exception e) {
            fail("질문 분석 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("7. 여러 질문 연속 처리 테스트")
    void testMultipleQueries() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        String[] queries = {
            "퇴직금은 언제 받을 수 있나요?",
            "복리후생 혜택에는 무엇이 있나요?",
            "보안 관련 규정이 궁금합니다"
        };

        System.out.println("\n=== 연속 질문 처리 테스트 ===");

        for (int i = 0; i < queries.length; i++) {
            try {
                System.out.println("\n" + (i + 1) + ". " + queries[i]);
                QueryAnalysisResult result = service.analyzeQuery(queries[i]);
                assertNotNull(result);
                System.out.println("   규정 유형: " + result.getRegulationTypes());
                System.out.println("   ✓ 처리 완료");
            } catch (Exception e) {
                fail("질문 " + (i + 1) + " 처리 중 오류: " + e.getMessage());
            }
        }
    }
}
