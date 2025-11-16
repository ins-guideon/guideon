package com.guideon;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.prompt.FewShotExample;
import com.guideon.service.QueryAnalysisService;
import com.guideon.util.PromptTemplate;
import com.guideon.util.TestResultRecorder;
import com.guideon.util.prompt.FewShotExampleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Few-shot 예제 적용 전후 비교 테스트
 * 질문 분석 및 답변 생성 품질을 비교하여 개선 효과를 측정합니다.
 */
@DisplayName("Few-shot 예제 전후 비교 테스트")
class FewShotComparisonTest {

    private QueryAnalysisService queryAnalysisService;
    private ConfigLoader config;
    private TestResultRecorder.TestResultSet beforeResults;
    private TestResultRecorder.TestResultSet afterResults;

    // 테스트 질문 세트
    private static final String[] TEST_QUESTIONS = {
        "경조사에 대한 규정을 알려줘",
        "연차 휴가는 몇 일인가요?",
        "출장비 신청은 어떻게 하나요?",
        "연차를 시간 단위로 사용할 수 있나요?",
        "퇴직금은 어떻게 계산하나요?",
        "경비 지급에서 예외가 되는 경우가 있나요?",
        "직원의 권리와 의무는 무엇인가요?"
    };

    @BeforeEach
    void setUp() {
        config = new ConfigLoader();
        try {
            queryAnalysisService = new QueryAnalysisService(config);
            System.out.println("✓ QueryAnalysisService 초기화 성공");
        } catch (IllegalStateException e) {
            System.err.println("⚠ API 키가 설정되지 않았습니다. 테스트를 건너뜁니다.");
            System.err.println("환경변수 GOOGLE_API_KEY 또는 application.properties 설정 필요");
        }

        beforeResults = new TestResultRecorder.TestResultSet();
        beforeResults.setTestName("Few-shot 예제 적용 전");
        beforeResults.setVersion("before");

        afterResults = new TestResultRecorder.TestResultSet();
        afterResults.setTestName("Few-shot 예제 적용 후");
        afterResults.setVersion("after");
    }

    /**
     * Few-shot 예제를 사용하지 않는 버전으로 질문 분석
     */
    private QueryAnalysisResult analyzeQueryWithoutFewShot(String question) {
        if (queryAnalysisService == null) {
            return null;
        }

        // Few-shot 예제 비활성화
        queryAnalysisService.setUseFewShotExamples(false);
        try {
            return queryAnalysisService.analyzeQuery(question);
        } finally {
            // 원래 상태로 복원
            queryAnalysisService.setUseFewShotExamples(true);
        }
    }

    @Test
    @DisplayName("Few-shot 예제 적용 전후 질문 분석 비교 테스트")
    void testQueryAnalysisComparison() {
        if (queryAnalysisService == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("Few-shot 예제 적용 전후 비교 테스트");
        System.out.println("========================================\n");

        // Before 테스트 (Few-shot 예제 없이)
        System.out.println("=== Before 테스트 (Few-shot 예제 없이) ===");
        for (String question : TEST_QUESTIONS) {
            try {
                long startTime = System.currentTimeMillis();
                QueryAnalysisResult result = analyzeQueryWithoutFewShot(question);
                long endTime = System.currentTimeMillis();

                if (result != null) {
                    TestResultRecorder.TestResult testResult = new TestResultRecorder.TestResult();
                    testResult.setQuestion(question);
                    testResult.setAnalysisResult(result);
                    testResult.setResponseTimeMs(endTime - startTime);
                    beforeResults.getResults().add(testResult);

                    System.out.println("\n질문: " + question);
                    System.out.println("  키워드: " + result.getKeywords());
                    System.out.println("  규정 유형: " + result.getRegulationTypes());
                    System.out.println("  의도: " + result.getIntent());
                    System.out.println("  검색 쿼리: " + result.getSearchQuery());
                    System.out.println("  응답 시간: " + (endTime - startTime) + "ms");
                }
            } catch (Exception e) {
                System.err.println("Before 테스트 실패: " + question);
                e.printStackTrace();
            }
        }

        // After 테스트 (Few-shot 예제 포함)
        System.out.println("\n=== After 테스트 (Few-shot 예제 포함) ===");
        for (String question : TEST_QUESTIONS) {
            try {
                long startTime = System.currentTimeMillis();
                QueryAnalysisResult result = queryAnalysisService.analyzeQuery(question);
                long endTime = System.currentTimeMillis();

                if (result != null) {
                    TestResultRecorder.TestResult testResult = new TestResultRecorder.TestResult();
                    testResult.setQuestion(question);
                    testResult.setAnalysisResult(result);
                    testResult.setResponseTimeMs(endTime - startTime);
                    afterResults.getResults().add(testResult);

                    System.out.println("\n질문: " + question);
                    System.out.println("  키워드: " + result.getKeywords());
                    System.out.println("  규정 유형: " + result.getRegulationTypes());
                    System.out.println("  의도: " + result.getIntent());
                    System.out.println("  검색 쿼리: " + result.getSearchQuery());
                    System.out.println("  응답 시간: " + (endTime - startTime) + "ms");
                }
            } catch (Exception e) {
                System.err.println("After 테스트 실패: " + question);
                e.printStackTrace();
            }
        }

        // 결과 저장
        TestResultRecorder.saveResults(beforeResults, "fewshot-comparison-before.json");
        TestResultRecorder.saveResults(afterResults, "fewshot-comparison-after.json");

        // 비교 리포트 생성
        Map<String, Object> comparison = TestResultRecorder.compareResults(beforeResults, afterResults);
        TestResultRecorder.saveComparisonReport(comparison, "fewshot-comparison-report.json");

        System.out.println("\n========================================");
        System.out.println("테스트 결과 저장 완료");
        System.out.println("========================================");
        System.out.println("- Before 결과: test-results/fewshot-comparison-before.json");
        System.out.println("- After 결과: test-results/fewshot-comparison-after.json");
        System.out.println("- 비교 리포트: test-results/fewshot-comparison-report.json");
        System.out.println("========================================\n");

        // 기본 검증
        assertFalse(beforeResults.getResults().isEmpty(), "Before 테스트 결과가 비어있으면 안됩니다");
        assertFalse(afterResults.getResults().isEmpty(), "After 테스트 결과가 비어있으면 안됩니다");
        assertEquals(beforeResults.getResults().size(), afterResults.getResults().size(), 
                     "Before와 After 테스트 결과 수가 같아야 합니다");
    }

    @Test
    @DisplayName("Few-shot 예제 적용 전후 답변 생성 비교 테스트")
    void testAnswerGenerationComparison() {
        if (queryAnalysisService == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("Few-shot 예제 적용 전후 답변 생성 비교 테스트");
        System.out.println("========================================\n");

        // 테스트용 컨텍스트
        String testContext = """
            취업규칙 제32조에 따르면:
            연차휴가는 근속년수에 따라 부여됩니다.
            - 1년 근속: 15일
            - 3년 근속: 16일
            - 5년 근속: 17일
            """;

        String testQuestion = "연차 휴가는 몇 일인가요?";
        String testIntent = "기준확인";

        // Before: Few-shot 예제 없이 답변 생성
        System.out.println("=== Before 테스트 (Few-shot 예제 없이) ===");
        String answerBefore = PromptTemplate.buildSimplePrompt(testQuestion, testContext);
        System.out.println("답변 프롬프트 길이: " + answerBefore.length() + " 문자");

        // After: Few-shot 예제 포함 답변 생성
        System.out.println("\n=== After 테스트 (Few-shot 예제 포함) ===");
        QueryAnalysisResult analysisResult = new QueryAnalysisResult();
        analysisResult.setIntent(testIntent);
        String answerAfter = PromptTemplate.buildPrompt(testQuestion, testContext, analysisResult);
        System.out.println("답변 프롬프트 길이: " + answerAfter.length() + " 문자");

        // Few-shot 예제가 포함되었는지 확인
        boolean hasFewShotExamples = answerAfter.contains("[Few-shot 예제]") || 
                                    answerAfter.contains("예제");
        System.out.println("Few-shot 예제 포함 여부: " + hasFewShotExamples);

        assertTrue(answerAfter.length() > answerBefore.length(), 
                  "Few-shot 예제가 포함된 프롬프트가 더 길어야 합니다");
    }

    @Test
    @DisplayName("Few-shot 예제 품질 검증 테스트")
    void testFewShotExampleQuality() {
        System.out.println("\n========================================");
        System.out.println("Few-shot 예제 품질 검증 테스트");
        System.out.println("========================================\n");

        // 질문 분석 예제 검증
        List<FewShotExample> queryExamples = FewShotExampleManager.getQueryAnalysisExamples(7);
        System.out.println("질문 분석 예제 수: " + queryExamples.size());
        assertTrue(queryExamples.size() >= 5, "질문 분석 예제는 최소 5개 이상이어야 합니다");

        for (FewShotExample example : queryExamples) {
            assertNotNull(example.getQuestion(), "질문이 null이면 안됩니다");
            assertNotNull(example.getAnalysisResult(), "분석 결과가 null이면 안됩니다");
            assertNotNull(example.getIntent(), "의도가 null이면 안됩니다");
            System.out.println("  - " + example.getQuestion() + " (의도: " + example.getIntent() + ")");
        }

        // 답변 생성 예제 검증
        String[] intents = {"기준확인", "절차설명", "가능여부", "예외상황", "계산방법", "권리의무", "정보조회"};
        for (String intent : intents) {
            List<FewShotExample> answerExamples = FewShotExampleManager.getAnswerGenerationExamples(intent, 7);
            System.out.println("\n" + intent + " 의도 예제 수: " + answerExamples.size());
            assertTrue(answerExamples.size() >= 5, intent + " 의도 예제는 최소 5개 이상이어야 합니다");

            for (FewShotExample example : answerExamples) {
                assertNotNull(example.getQuestion(), "질문이 null이면 안됩니다");
                assertNotNull(example.getAnswer(), "답변이 null이면 안됩니다");
                assertEquals(intent, example.getIntent(), "의도가 일치해야 합니다");
            }
        }

        System.out.println("\n✓ Few-shot 예제 품질 검증 완료");
    }
}

