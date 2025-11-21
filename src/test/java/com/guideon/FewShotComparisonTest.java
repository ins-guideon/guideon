package com.guideon;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationSearchResult;
import com.guideon.model.prompt.FewShotExample;
import com.guideon.service.QueryAnalysisService;
import com.guideon.service.RegulationSearchService;
import com.guideon.util.AnswerQualityEnhancer;
import com.guideon.util.TestResultRecorder;
import com.guideon.util.prompt.FewShotExampleManager;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Few-shot 예제 적용 전후 비교 테스트
 * 질문 분석 및 답변 생성 품질을 비교하여 개선 효과를 측정합니다.
 */
@DisplayName("Few-shot 예제 전후 비교 테스트")
class FewShotComparisonTest {

    private QueryAnalysisService queryAnalysisService;
    private RegulationSearchService regulationSearchService;
    private ConfigLoader config;
    private TestResultRecorder.TestResultSet beforeResults;
    private TestResultRecorder.TestResultSet afterResults;
    private TestResultRecorder.TestResultSet beforeAnswerResults;
    private TestResultRecorder.TestResultSet afterAnswerResults;

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
            regulationSearchService = new RegulationSearchService(config, null);
            System.out.println("✓ QueryAnalysisService 초기화 성공");
            System.out.println("✓ RegulationSearchService 초기화 성공");
            
            // 테스트용 샘플 문서 인덱싱
            indexSampleDocuments();
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
        
        beforeAnswerResults = new TestResultRecorder.TestResultSet();
        beforeAnswerResults.setTestName("Few-shot 예제 적용 전 (답변 생성)");
        beforeAnswerResults.setVersion("before");

        afterAnswerResults = new TestResultRecorder.TestResultSet();
        afterAnswerResults.setTestName("Few-shot 예제 적용 후 (답변 생성)");
        afterAnswerResults.setVersion("after");
    }
    
    /**
     * 테스트용 샘플 규정 문서 인덱싱
     */
    private void indexSampleDocuments() {
        if (regulationSearchService == null) {
            return;
        }
        
        // 샘플 취업규칙 문서
        Document vacationDoc = Document.from("""
            취업규칙 제32조 (연차휴가)
            연차휴가는 근속년수에 따라 다음과 같이 부여됩니다.
            - 1년 근속: 15일
            - 3년 근속: 16일
            - 5년 근속: 17일
            - 이후 2년마다 1일씩 가산 (최대 25일)
            
            취업규칙 제16조 (연차휴가 사용)
            연차휴가는 다음과 같이 분할 사용할 수 있습니다:
            - 1일 단위 사용 (기본)
            - 반일(4시간) 단위 사용
            - 시간 단위 사용 (최소 1시간, 연간 최대 40시간)
            """, Metadata.from("regulation_type", "취업규칙"));
        regulationSearchService.indexDocument(vacationDoc, "취업규칙");
        
        // 샘플 복리후생비규정 문서
        Document welfareDoc = Document.from("""
            복리후생비규정 제10조 (경조금 지급)
            경조금 지급 기준은 다음과 같습니다:
            - 본인 결혼: 100만원
            - 자녀 결혼: 50만원
            - 본인/배우자 사망: 200만원
            - 부모 사망: 100만원
            
            복리후생비규정 제12조 (경조휴가)
            경조휴가는 다음과 같이 부여됩니다:
            - 본인 결혼: 5일
            - 자녀 결혼: 1일
            - 본인/배우자 사망: 5일
            - 부모 사망: 3일
            """, Metadata.from("regulation_type", "복리후생비규정"));
        regulationSearchService.indexDocument(welfareDoc, "복리후생비규정");
        
        System.out.println("✓ 샘플 문서 인덱싱 완료");
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
        for (int i = 0; i < TEST_QUESTIONS.length; i++) {
            String question = TEST_QUESTIONS[i];
            try {
                // Rate limit 방지를 위한 지연 (무료 티어: 분당 10개 요청)
                if (i > 0) {
                    Thread.sleep(7000); // 7초 대기 (분당 약 8-9개 요청)
                }
                
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
        if (queryAnalysisService == null || regulationSearchService == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정 또는 서비스 초기화 실패");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("Few-shot 예제 적용 전후 답변 생성 비교 테스트");
        System.out.println("========================================\n");

        // 테스트 질문 세트 (답변 생성에 적합한 질문들)
        String[] answerTestQuestions = {
            "연차 휴가는 몇 일인가요?",
            "경조사에 대한 규정을 알려줘",
            "출장비 신청은 어떻게 하나요?"
        };

        // Before 테스트 (Few-shot 예제 없이)
        System.out.println("=== Before 테스트 (Few-shot 예제 없이) ===");
        for (String question : answerTestQuestions) {
            try {
                long startTime = System.currentTimeMillis();
                
                // 질문 분석 (Few-shot 없이)
                QueryAnalysisResult analysis = analyzeQueryWithoutFewShot(question);
                if (analysis == null) {
                    continue;
                }
                
                // 답변 생성 (Few-shot 예제 비활성화)
                com.guideon.util.PromptTemplate.setUseFewShotExamples(false);
                RegulationSearchResult searchResult;
                try {
                    searchResult = regulationSearchService.search(analysis);
                } finally {
                    // 원래 상태로 복원
                    com.guideon.util.PromptTemplate.setUseFewShotExamples(true);
                }
                
                long endTime = System.currentTimeMillis();
                
                if (searchResult != null && searchResult.getAnswer() != null) {
                    // 답변 품질 분석
                    String answer = searchResult.getAnswer();
                    double qualityScore = AnswerQualityEnhancer.calculateAnswerQualityScore(answer, analysis);
                    List<String> articleRefs = AnswerQualityEnhancer.extractReferencedArticles(answer);
                    
                    TestResultRecorder.TestResult testResult = new TestResultRecorder.TestResult();
                    testResult.setQuestion(question);
                    testResult.setAnalysisResult(analysis);
                    testResult.setAnswer(answer);
                    testResult.setResponseTimeMs(endTime - startTime);
                    testResult.getMetadata().put("qualityScore", qualityScore);
                    testResult.getMetadata().put("articleReferences", articleRefs.size());
                    testResult.getMetadata().put("answerLength", answer.length());
                    beforeAnswerResults.getResults().add(testResult);
                    
                    System.out.println("\n질문: " + question);
                    System.out.println("  답변 길이: " + answer.length() + " 문자");
                    System.out.println("  품질 점수: " + String.format("%.3f", qualityScore));
                    System.out.println("  조항 인용: " + articleRefs.size() + "개");
                    System.out.println("  응답 시간: " + (endTime - startTime) + "ms");
                }
            } catch (Exception e) {
                System.err.println("Before 답변 생성 테스트 실패: " + question);
                e.printStackTrace();
            }
        }

        // After 테스트 (Few-shot 예제 포함)
        System.out.println("\n=== After 테스트 (Few-shot 예제 포함) ===");
        for (String question : answerTestQuestions) {
            try {
                long startTime = System.currentTimeMillis();
                
                // 질문 분석 (Few-shot 포함)
                QueryAnalysisResult analysis = queryAnalysisService.analyzeQuery(question);
                if (analysis == null) {
                    continue;
                }
                
                // 답변 생성 (Few-shot 예제 포함)
                RegulationSearchResult searchResult = regulationSearchService.search(analysis);
                
                long endTime = System.currentTimeMillis();
                
                if (searchResult != null && searchResult.getAnswer() != null) {
                    // 답변 품질 분석
                    String answer = searchResult.getAnswer();
                    double qualityScore = AnswerQualityEnhancer.calculateAnswerQualityScore(answer, analysis);
                    List<String> articleRefs = AnswerQualityEnhancer.extractReferencedArticles(answer);
                    
                    TestResultRecorder.TestResult testResult = new TestResultRecorder.TestResult();
                    testResult.setQuestion(question);
                    testResult.setAnalysisResult(analysis);
                    testResult.setAnswer(answer);
                    testResult.setResponseTimeMs(endTime - startTime);
                    testResult.getMetadata().put("qualityScore", qualityScore);
                    testResult.getMetadata().put("articleReferences", articleRefs.size());
                    testResult.getMetadata().put("answerLength", answer.length());
                    afterAnswerResults.getResults().add(testResult);
                    
                    System.out.println("\n질문: " + question);
                    System.out.println("  답변 길이: " + answer.length() + " 문자");
                    System.out.println("  품질 점수: " + String.format("%.3f", qualityScore));
                    System.out.println("  조항 인용: " + articleRefs.size() + "개");
                    System.out.println("  응답 시간: " + (endTime - startTime) + "ms");
                }
            } catch (Exception e) {
                System.err.println("After 답변 생성 테스트 실패: " + question);
                e.printStackTrace();
            }
        }

        // 결과 저장
        if (!beforeAnswerResults.getResults().isEmpty()) {
            TestResultRecorder.saveResults(beforeAnswerResults, "fewshot-answer-comparison-before.json");
        }
        if (!afterAnswerResults.getResults().isEmpty()) {
            TestResultRecorder.saveResults(afterAnswerResults, "fewshot-answer-comparison-after.json");
        }

        // 비교 리포트 생성
        if (!beforeAnswerResults.getResults().isEmpty() && !afterAnswerResults.getResults().isEmpty()) {
            Map<String, Object> answerComparison = TestResultRecorder.compareResults(beforeAnswerResults, afterAnswerResults);
            TestResultRecorder.saveComparisonReport(answerComparison, "fewshot-answer-comparison-report.json");
            
            System.out.println("\n========================================");
            System.out.println("답변 생성 비교 결과 저장 완료");
            System.out.println("========================================");
            System.out.println("- Before 결과: test-results/fewshot-answer-comparison-before.json");
            System.out.println("- After 결과: test-results/fewshot-answer-comparison-after.json");
            System.out.println("- 비교 리포트: test-results/fewshot-answer-comparison-report.json");
            System.out.println("========================================\n");
        }

        // 기본 검증
        assertFalse(beforeAnswerResults.getResults().isEmpty() || afterAnswerResults.getResults().isEmpty(), 
                   "답변 생성 테스트 결과가 있어야 합니다");
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

