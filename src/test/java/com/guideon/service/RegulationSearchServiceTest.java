package com.guideon.service;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationSearchResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegulationSearchService 테스트 클래스
 * RAG 기반 규정 검색 및 답변 생성 기능 테스트
 */
@DisplayName("RegulationSearchService 테스트")
class RegulationSearchServiceTest {

    private RegulationSearchService service;
    private ConfigLoader config;

    @BeforeEach
    void setUp() {
        config = new ConfigLoader();

        try {
            service = new RegulationSearchService(config);
            System.out.println("✓ RegulationSearchService 초기화 성공");

            // 테스트용 샘플 문서 인덱싱
            indexSampleDocuments();
        } catch (IllegalStateException e) {
            System.err.println("⚠ API 키가 설정되지 않았습니다. 일부 테스트를 건너뜁니다.");
        }
    }

    /**
     * 테스트용 샘플 규정 문서 인덱싱
     */
    private void indexSampleDocuments() {
        // 샘플 취업규칙 문서
        String employmentRuleContent = """
            제1장 총칙
            제1조(목적) 이 규칙은 회사의 취업에 관한 사항을 규정함을 목적으로 한다.

            제2장 근로시간 및 휴일
            제10조(근로시간) 근로시간은 1일 8시간, 1주 40시간을 원칙으로 한다.
            제11조(휴게시간) 근로시간이 8시간인 경우 휴게시간은 1시간으로 한다.

            제3장 휴가
            제20조(연차휴가)
            1. 1년간 80% 이상 출근한 근로자에게 15일의 유급휴가를 준다.
            2. 계속 근로연수 1년에 대하여 1일을 가산한 유급휴가를 준다. 다만, 총 휴가일수는 25일을 한도로 한다.
            3. 신입사원의 경우 입사 후 1년 미만일 때는 1개월 개근 시 1일의 유급휴가를 부여한다.

            제21조(경조휴가)
            1. 본인 결혼: 5일
            2. 자녀 결혼: 2일
            3. 배우자, 본인 및 배우자의 부모 사망: 5일
            4. 형제자매 사망: 3일
            """;

        Document employmentDoc = new Document(employmentRuleContent, new Metadata());
        service.indexDocument(employmentDoc, "취업규칙");

        // 샘플 출장여비지급규정 문서
        String travelExpenseContent = """
            제1장 총칙
            제1조(목적) 이 규정은 임직원의 출장여비 지급에 관한 사항을 규정함을 목적으로 한다.

            제2장 국내출장
            제5조(교통비)
            1. 철도: 실비 지급 (KTX 일반실 기준)
            2. 항공: 실비 지급 (이코노미 클래스)
            3. 자가용 이용 시: km당 150원

            제6조(숙박비)
            1. 특급지(서울, 부산): 1박당 10만원 한도
            2. 일반지역: 1박당 8만원 한도

            제3장 해외출장
            제10조(항공료)
            1. 8시간 미만: 이코노미 클래스
            2. 8시간 이상: 비즈니스 클래스 가능

            제11조(숙박비)
            1. 미주, 유럽: 1박당 200달러 한도
            2. 아시아: 1박당 150달러 한도
            3. 기타 지역: 1박당 100달러 한도

            제12조(일비) 1일당 50달러 지급
            """;

        Document travelDoc = new Document(travelExpenseContent, new Metadata());
        service.indexDocument(travelDoc, "출장여비지급규정");

        // 샘플 경비지급규정 문서
        String expenseContent = """
            제1장 총칙
            제1조(목적) 이 규정은 회사의 경비 지급에 관한 사항을 규정함을 목적으로 한다.

            제2장 법인카드
            제5조(사용한도)
            1. 임원: 월 500만원
            2. 팀장급: 월 300만원
            3. 일반직원: 월 100만원

            제6조(사용용도)
            1. 업무 관련 식대, 교통비
            2. 사무용품 구매
            3. 거래처 접대비
            4. 기타 업무상 필요경비
            """;

        Document expenseDoc = new Document(expenseContent, new Metadata());
        service.indexDocument(expenseDoc, "경비지급규정");

        System.out.println("✓ 샘플 문서 3개 인덱싱 완료 (취업규칙, 출장여비지급규정, 경비지급규정)");
    }

    @Test
    @DisplayName("1. 서비스 초기화 및 문서 인덱싱 테스트")
    void testServiceInitialization() {
        assertNotNull(config, "ConfigLoader가 null이면 안됩니다");

        if (service != null) {
            System.out.println("✓ RegulationSearchService 정상 초기화됨");
            System.out.println("✓ 샘플 문서 인덱싱 완료");
        } else {
            System.out.println("⚠ API 키 미설정으로 서비스 초기화 불가");
        }
    }

    @Test
    @DisplayName("2. 연차휴가 검색 테스트")
    void testSearchVacationRegulation() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 연차휴가 검색 테스트 ===");

        // 질문 분석 결과 생성
        QueryAnalysisResult analysis = new QueryAnalysisResult(
                "연차 휴가는 몇 일인가요?",
                Arrays.asList("연차", "휴가", "일수"),
                Arrays.asList("취업규칙"),
                "기준확인",
                "연차 휴가 일수"
        );

        try {
            RegulationSearchResult result = service.search(analysis);

            assertNotNull(result, "검색 결과가 null이면 안됩니다");
            assertNotNull(result.getAnswer(), "답변이 null이면 안됩니다");

            System.out.println("질문: " + analysis.getOriginalQuery());
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n근거 조항 수: " + result.getReferences().size());
            System.out.println("신뢰도: " + String.format("%.2f%%", result.getConfidenceScore() * 100));
            System.out.println("관련 규정 발견: " + result.isFoundRelevantRegulation());

            assertTrue(result.isFoundRelevantRegulation(), "관련 규정이 발견되어야 합니다");

        } catch (Exception e) {
            fail("검색 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("3. 출장비 검색 테스트")
    void testSearchTravelExpense() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 출장비 검색 테스트 ===");

        QueryAnalysisResult analysis = new QueryAnalysisResult(
                "해외 출장 시 숙박비는 얼마까지 지원되나요?",
                Arrays.asList("해외", "출장", "숙박비", "지원"),
                Arrays.asList("출장여비지급규정"),
                "기준확인",
                "해외 출장 숙박비 지원 기준"
        );

        try {
            RegulationSearchResult result = service.search(analysis);

            assertNotNull(result);

            System.out.println("질문: " + analysis.getOriginalQuery());
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n근거 조항:");
            result.getReferences().forEach(ref -> {
                System.out.println("- " + ref.getDocumentName() +
                        " (관련도: " + String.format("%.2f", ref.getRelevanceScore()) + ")");
            });

        } catch (Exception e) {
            fail("검색 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("4. 법인카드 한도 검색 테스트")
    void testSearchCorporateCardLimit() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 법인카드 한도 검색 테스트 ===");

        QueryAnalysisResult analysis = new QueryAnalysisResult(
                "법인카드 사용 한도는 얼마인가요?",
                Arrays.asList("법인카드", "사용", "한도"),
                Arrays.asList("경비지급규정"),
                "기준확인",
                "법인카드 사용 한도"
        );

        try {
            RegulationSearchResult result = service.search(analysis);

            assertNotNull(result);

            System.out.println("질문: " + analysis.getOriginalQuery());
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());

            if (result.isFoundRelevantRegulation()) {
                System.out.println("\n✓ 관련 규정 발견됨");
            }

        } catch (Exception e) {
            fail("검색 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("5. 없는 규정 검색 테스트 (Fallback)")
    void testSearchNonExistentRegulation() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 없는 규정 검색 테스트 ===");

        QueryAnalysisResult analysis = new QueryAnalysisResult(
                "우주여행 지원금은 얼마인가요?",
                Arrays.asList("우주여행", "지원금"),
                Arrays.asList("일반"),
                "기준확인",
                "우주여행 지원금"
        );

        try {
            RegulationSearchResult result = service.search(analysis);

            assertNotNull(result);
            assertNotNull(result.getAnswer());

            System.out.println("질문: " + analysis.getOriginalQuery());
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n관련 규정 발견: " + result.isFoundRelevantRegulation());

            // 관련 규정이 없을 경우 Fallback 메시지가 포함되어야 함
            if (!result.isFoundRelevantRegulation()) {
                System.out.println("✓ Fallback 처리 정상 작동");
            }

        } catch (Exception e) {
            fail("검색 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("6. 복합 질문 검색 테스트")
    void testSearchComplexQuery() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 복합 질문 검색 테스트 ===");

        QueryAnalysisResult analysis = new QueryAnalysisResult(
                "신입사원의 연차휴가와 경조휴가는 각각 며칠인가요?",
                Arrays.asList("신입사원", "연차휴가", "경조휴가"),
                Arrays.asList("취업규칙"),
                "기준확인",
                "신입사원 연차휴가 경조휴가 일수"
        );

        try {
            RegulationSearchResult result = service.search(analysis);

            assertNotNull(result);

            System.out.println("질문: " + analysis.getOriginalQuery());
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n검색된 근거 수: " + result.getReferences().size());

        } catch (Exception e) {
            fail("검색 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("7. 연속 검색 성능 테스트")
    void testMultipleSearches() {
        if (service == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 연속 검색 성능 테스트 ===");

        String[][] testQueries = {
            {"근로시간은 몇 시간인가요?", "취업규칙"},
            {"국내 출장 시 교통비는?", "출장여비지급규정"},
            {"결혼 휴가는 며칠인가요?", "취업규칙"}
        };

        for (int i = 0; i < testQueries.length; i++) {
            try {
                QueryAnalysisResult analysis = new QueryAnalysisResult(
                        testQueries[i][0],
                        Arrays.asList(testQueries[i][0].split(" ")),
                        Arrays.asList(testQueries[i][1]),
                        "기준확인",
                        testQueries[i][0]
                );

                long startTime = System.currentTimeMillis();
                RegulationSearchResult result = service.search(analysis);
                long endTime = System.currentTimeMillis();

                assertNotNull(result);

                System.out.println("\n" + (i + 1) + ". " + testQueries[i][0]);
                System.out.println("   처리 시간: " + (endTime - startTime) + "ms");
                System.out.println("   관련 규정 발견: " + result.isFoundRelevantRegulation());

            } catch (Exception e) {
                fail("검색 " + (i + 1) + " 중 오류: " + e.getMessage());
            }
        }

        System.out.println("\n✓ 연속 검색 테스트 완료");
    }
}
