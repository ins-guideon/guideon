package com.guideon;

import com.guideon.config.ConfigLoader;
import com.guideon.model.RegulationSearchResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegulationQASystem 통합 테스트 클래스
 * 전체 시스템의 End-to-End 테스트
 */
@DisplayName("RegulationQASystem 통합 테스트")
class RegulationQASystemTest {

    private RegulationQASystem system;

    @BeforeEach
    void setUp() {
        try {
            system = new RegulationQASystem();
            System.out.println("✓ RegulationQASystem 초기화 성공");

            // 테스트용 샘플 문서 업로드
            uploadSampleDocuments();
        } catch (IllegalStateException e) {
            System.err.println("⚠ API 키가 설정되지 않았습니다. 일부 테스트를 건너뜁니다.");
            System.err.println("환경변수 GOOGLE_API_KEY 또는 application.properties 설정 필요");
        }
    }

    /**
     * 테스트용 샘플 문서 업로드
     */
    private void uploadSampleDocuments() {
        // 취업규칙 문서
        String employmentRuleContent = """
            [취업규칙]

            제20조(연차휴가)
            1. 1년간 80% 이상 출근한 근로자에게 15일의 유급휴가를 준다.
            2. 계속 근로연수 1년에 대하여 1일을 가산한 유급휴가를 준다.
            3. 총 휴가일수는 25일을 한도로 한다.
            4. 신입사원의 경우 입사 후 1년 미만일 때는 1개월 개근 시 1일의 유급휴가를 부여한다.

            제21조(경조휴가)
            1. 본인 결혼: 5일
            2. 자녀 결혼: 2일
            3. 배우자, 본인 및 배우자의 부모 사망: 5일

            제10조(근로시간)
            1. 근로시간은 1일 8시간, 1주 40시간을 원칙으로 한다.
            2. 점심시간은 12시부터 13시까지로 한다.
            """;

        Document employmentDoc = new Document(employmentRuleContent, new Metadata());
        system.uploadRegulationDocument(employmentDoc, "취업규칙");

        // 출장여비지급규정 문서
        String travelExpenseContent = """
            [출장여비지급규정]

            제11조(해외출장 숙박비)
            1. 미주, 유럽: 1박당 200달러 한도
            2. 아시아: 1박당 150달러 한도
            3. 기타 지역: 1박당 100달러 한도

            제10조(해외출장 항공료)
            1. 8시간 미만 비행: 이코노미 클래스
            2. 8시간 이상 비행: 비즈니스 클래스 가능

            제12조(일비)
            1. 해외출장 시 1일당 50달러를 일비로 지급한다.

            제6조(국내출장 숙박비)
            1. 특급지(서울, 부산): 1박당 10만원 한도
            2. 일반지역: 1박당 8만원 한도
            """;

        Document travelDoc = new Document(travelExpenseContent, new Metadata());
        system.uploadRegulationDocument(travelDoc, "출장여비지급규정");

        System.out.println("✓ 샘플 문서 업로드 완료 (취업규칙, 출장여비지급규정)");
    }

    /**
     * Document 객체 생성을 위한 헬퍼 메서드
     */
    private void uploadRegulationDocument(RegulationQASystem system, String content, String regulationType) {
        Document doc = new Document(content, new Metadata());
        system.uploadRegulationDocument(doc, regulationType);
    }

    @Test
    @DisplayName("1. 시스템 초기화 테스트")
    void testSystemInitialization() {
        if (system != null) {
            System.out.println("✓ 시스템 정상 초기화됨");
            String status = system.getSystemStatus();
            assertNotNull(status);
            System.out.println("\n" + status);
        } else {
            System.out.println("⚠ API 키 미설정으로 시스템 초기화 불가");
        }
    }

    @Test
    @DisplayName("2. 연차휴가 질의응답 테스트")
    void testVacationQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 연차휴가 질의응답 테스트 ===");
        String question = "연차 휴가는 몇 일인가요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result, "결과가 null이면 안됩니다");
            assertNotNull(result.getAnswer(), "답변이 null이면 안됩니다");

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n근거 조항 수: " + result.getReferences().size());
            System.out.println("신뢰도: " + String.format("%.2f%%", result.getConfidenceScore() * 100));

            if (result.getReferences().size() > 0) {
                System.out.println("\n주요 근거:");
                result.getReferences().stream()
                        .limit(2)
                        .forEach(ref -> {
                            System.out.println("- " + ref.getDocumentName() +
                                    " (관련도: " + String.format("%.2f", ref.getRelevanceScore()) + ")");
                        });
            }

            assertTrue(result.getAnswer().length() > 0, "답변 내용이 있어야 합니다");

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("3. 해외출장 숙박비 질의응답 테스트")
    void testOverseasTravelQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 해외출장 숙박비 질의응답 테스트 ===");
        String question = "미국 출장 시 숙박비는 얼마까지 지원되나요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result);

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());

            if (result.isFoundRelevantRegulation()) {
                System.out.println("\n✓ 관련 규정 발견됨");
                System.out.println("근거 조항 수: " + result.getReferences().size());
            }

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("4. 신입사원 연차 질의응답 테스트")
    void testNewEmployeeVacationQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 신입사원 연차 질의응답 테스트 ===");
        String question = "신입사원은 입사 첫 해에 연차를 몇 일 받을 수 있나요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result);

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n신뢰도: " + String.format("%.2f%%", result.getConfidenceScore() * 100));

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("5. 근로시간 질의응답 테스트")
    void testWorkingHoursQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 근로시간 질의응답 테스트 ===");
        String question = "하루 근로시간은 몇 시간인가요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result);

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("6. 경조휴가 질의응답 테스트")
    void testFamilyEventLeaveQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 경조휴가 질의응답 테스트 ===");
        String question = "본인 결혼 시 휴가는 며칠인가요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result);

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("7. 없는 규정 질의응답 테스트 (Fallback)")
    void testNonExistentRegulationQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 없는 규정 질의응답 테스트 ===");
        String question = "우주여행 지원금은 얼마인가요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result);

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n관련 규정 발견: " + result.isFoundRelevantRegulation());

            if (!result.isFoundRelevantRegulation()) {
                System.out.println("✓ Fallback 메시지 정상 작동");
                assertTrue(result.getAnswer().contains("찾을 수 없습니다") ||
                          result.getAnswer().contains("없습니다"),
                          "Fallback 메시지가 포함되어야 합니다");
            }

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("8. 복합 질문 질의응답 테스트")
    void testComplexQuestion() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 복합 질문 질의응답 테스트 ===");
        String question = "입사 1년차 직원의 연차휴가와 근로시간은 각각 어떻게 되나요?";

        try {
            RegulationSearchResult result = system.askQuestion(question);

            assertNotNull(result);

            System.out.println("질문: " + question);
            System.out.println("\n답변:");
            System.out.println(result.getAnswer());
            System.out.println("\n근거 조항 수: " + result.getReferences().size());

        } catch (Exception e) {
            fail("질의응답 중 오류 발생: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("9. 연속 질의응답 테스트")
    void testMultipleQuestions() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 연속 질의응답 테스트 ===");

        String[] questions = {
            "연차는 총 몇 일까지 받을 수 있나요?",
            "해외 출장 시 일비는 얼마인가요?",
            "아시아 출장 시 숙박비는 얼마까지 지원되나요?"
        };

        for (int i = 0; i < questions.length; i++) {
            try {
                System.out.println("\n[질문 " + (i + 1) + "] " + questions[i]);

                long startTime = System.currentTimeMillis();
                RegulationSearchResult result = system.askQuestion(questions[i]);
                long endTime = System.currentTimeMillis();

                assertNotNull(result);

                System.out.println("답변: " + result.getAnswer());
                System.out.println("처리 시간: " + (endTime - startTime) + "ms");
                System.out.println("✓ 완료");

            } catch (Exception e) {
                fail("질문 " + (i + 1) + " 처리 중 오류: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("10. 시스템 상태 확인 테스트")
    void testSystemStatus() {
        if (system == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n=== 시스템 상태 확인 테스트 ===");

        String status = system.getSystemStatus();
        assertNotNull(status, "시스템 상태가 null이면 안됩니다");
        assertTrue(status.contains("Query Analysis"), "Query Analysis 상태가 포함되어야 합니다");
        assertTrue(status.contains("Search Service"), "Search Service 상태가 포함되어야 합니다");

        System.out.println(status);
    }
}
