package com.guideon;

import com.guideon.config.ConfigLoader;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationSearchResult;
import com.guideon.model.prompt.IntentMetadata;
import com.guideon.service.QueryAnalysisService;
import com.guideon.service.RegulationSearchService;
import com.guideon.util.AnswerQualityEnhancer;
import com.guideon.util.TestResultRecorder;
import com.guideon.util.prompt.PromptMetadataManager;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 메타데이터 적용 전후 비교 테스트
 * 메타데이터가 답변 생성 품질에 미치는 영향을 측정합니다.
 */
@DisplayName("메타데이터 전후 비교 테스트")
class MetadataComparisonTest {

    private QueryAnalysisService queryAnalysisService;
    private RegulationSearchService regulationSearchService;
    private ConfigLoader config;
    private TestResultRecorder.TestResultSet beforeAnswerResults;
    private TestResultRecorder.TestResultSet afterAnswerResults;

    // 의도별 테스트 질문 세트
    private static final Map<String, String[]> TEST_QUESTIONS_BY_INTENT = Map.of(
        "기준확인", new String[]{
            "연차 휴가는 몇 일인가요?",
            "경조금은 얼마나 받을 수 있나요?",
            "출장 일비는 얼마인가요?",
            "야근 수당은 시간당 얼마인가요?"
        },
        "절차설명", new String[]{
            "출장비 신청은 어떻게 하나요?",
            "연차휴가 신청 절차는 어떻게 되나요?",
            "경조휴가 신청은 어떻게 하나요?",
            "법인카드 발급 신청은 어떻게 하나요?"
        },
        "가능여부", new String[]{
            "연차를 시간 단위로 사용할 수 있나요?",
            "경조휴가를 연차로 대체할 수 있나요?",
            "출장비를 선지급받을 수 있나요?",
            "연차를 다음 해로 이월할 수 있나요?"
        },
        "예외상황", new String[]{
            "경비 지급에서 예외가 되는 경우가 있나요?",
            "연차 사용에 예외가 있나요?",
            "출장비 정산에 예외가 있나요?",
            "경조휴가 사용에 예외가 있나요?"
        },
        "계산방법", new String[]{
            "퇴직금은 어떻게 계산하나요?",
            "야근 수당은 어떻게 계산하나요?",
            "연차 일수는 어떻게 계산하나요?",
            "출장비는 어떻게 계산하나요?"
        },
        "권리의무", new String[]{
            "직원의 권리와 의무는 무엇인가요?",
            "회사의 권리와 의무는 무엇인가요?",
            "직원이 휴가를 거부당할 수 있나요?",
            "회사가 임금을 지급하지 않으면 어떻게 하나요?"
        },
        "정보조회", new String[]{
            "경조사에 대한 규정을 알려줘",
            "출장 규정에 대해 알려줘",
            "법인카드 사용 규정을 알려줘",
            "건강검진에 대해 알려줘"
        }
    );

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

        beforeAnswerResults = new TestResultRecorder.TestResultSet();
        beforeAnswerResults.setTestName("메타데이터 적용 전 (답변 생성)");
        beforeAnswerResults.setVersion("before");

        afterAnswerResults = new TestResultRecorder.TestResultSet();
        afterAnswerResults.setTestName("메타데이터 적용 후 (답변 생성)");
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
            
            취업규칙 제33조 (연차휴가 이월)
            연차의 50%까지 다음 해 3월 말까지 이월 가능합니다.
            나머지 50%는 당해 연도 내 사용해야 하며 미사용 시 소멸됩니다.
            
            취업규칙 제45조 (야근 수당)
            야근 수당은 기본급의 150%를 지급합니다.
            - 평일 야근: 시간당 기본급의 150%
            - 휴일 야근: 시간당 기본급의 200%
            - 심야 근무(22시 이후): 추가 50% 가산
            최소 지급 단위는 1시간입니다.
            
            취업규칙 제3조 (직원의 권리와 의무)
            직원의 권리:
            1. 정당한 임금 수령권
            2. 휴가 사용권
            3. 근로 환경 보호권
            
            직원의 의무:
            1. 성실 근무 의무
            2. 비밀 유지 의무
            3. 회사 재산 보호 의무
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
            
            복리후생비규정 제16조 (경조휴가 신청)
            경조휴가 신청 절차는 다음과 같습니다:
            1. 경조사 발생 시 즉시 신청
            2. 증빙서류 제출
            3. 승인 및 사용 (승인일로부터 30일 이내 사용)
            
            복리후생비규정 제22조 (건강검진)
            건강검진은 매년 1회 실시됩니다.
            - 정기 건강검진: 매년 1회 (회사 부담)
            - 대상: 전 직원
            - 검진 항목: 기본 검진 + 직종별 추가 검진
            """, Metadata.from("regulation_type", "복리후생비규정"));
        regulationSearchService.indexDocument(welfareDoc, "복리후생비규정");
        
        // 샘플 출장여비지급규정 문서
        Document travelDoc = Document.from("""
            출장여비지급규정 제6조 (출장비 구성)
            출장비는 교통비, 숙박비, 일비로 구성됩니다.
            
            출장여비지급규정 제8조 (일비)
            출장 일비는 지역에 따라 차등 지급됩니다:
            - 수도권: 5만원
            - 광역시: 4만원
            - 기타 지역: 3만원
            
            출장여비지급규정 제9조 (선지급)
            해외 출장 또는 장기 출장(3일 이상)의 경우 사전 신청을 통해 선지급이 가능합니다.
            
            출장여비지급규정 제10조 (출장비 정산)
            출장비 정산 절차는 다음과 같습니다:
            1. 출장 후 7일 이내에 그룹웨어에서 신청
            2. 증빙서류 첨부
            3. 결재 진행 (부서장 결재 후 경리팀으로 전송)
            4. 통상 3~5일 내 지급
            """, Metadata.from("regulation_type", "출장여비지급규정"));
        regulationSearchService.indexDocument(travelDoc, "출장여비지급규정");
        
        // 샘플 경비지급규정 문서
        Document expenseDoc = Document.from("""
            경비지급규정 제7조 (경비 지급 예외)
            다음의 경우 경비 지급이 제한되거나 불가능합니다:
            1. 증빙서류 미제출
            2. 개인 용도 지출
            3. 한도 초과 (단, 사전 승인 시 예외 가능)
            
            경비지급규정 제12조 (법인카드 한도)
            법인카드 한도는 직급과 용도에 따라 다릅니다:
            - 임원: 월 500만원
            - 부장: 월 300만원
            - 차장: 월 200만원
            - 과장: 월 100만원
            - 대리 이하: 월 50만원
            
            경비지급규정 제11조 (법인카드 발급)
            법인카드 발급 신청 절차:
            1. 발급 신청서 작성
            2. 부서장 승인
            3. 카드 발급 및 수령 (약 1주일 소요)
            """, Metadata.from("regulation_type", "경비지급규정"));
        regulationSearchService.indexDocument(expenseDoc, "경비지급규정");
        
        // 샘플 임원퇴직금지급규정 문서
        Document retirementDoc = Document.from("""
            임원퇴직금지급규정 제5조 (퇴직금 계산)
            퇴직금은 다음과 같이 계산합니다:
            퇴직금 = 평균임금 × 근속년수
            
            평균임금 계산:
            - 퇴직일 이전 3개월간의 임금 총액 ÷ 3개월
            - 기본급 + 각종 수당 포함
            
            근속년수 계산:
            - 입사일부터 퇴직일까지의 기간
            - 1년 미만은 월 단위로 계산 (1개월 = 1/12년)
            """, Metadata.from("regulation_type", "임원퇴직금지급규정"));
        regulationSearchService.indexDocument(retirementDoc, "임원퇴직금지급규정");
        
        System.out.println("✓ 샘플 문서 인덱싱 완료");
    }

    @Test
    @DisplayName("메타데이터 적용 전후 답변 생성 비교 테스트")
    void testAnswerGenerationComparison() {
        if (queryAnalysisService == null || regulationSearchService == null) {
            System.out.println("⚠ 테스트 건너뜀: API 키 미설정 또는 서비스 초기화 실패");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("메타데이터 적용 전후 답변 생성 비교 테스트");
        System.out.println("========================================\n");

        // 모든 의도별 질문 수집
        List<String> allQuestions = new ArrayList<>();
        for (String[] questions : TEST_QUESTIONS_BY_INTENT.values()) {
            allQuestions.addAll(Arrays.asList(questions));
        }
        
        // 테스트 질문 수 제한 (API 비용 및 시간 고려)
        String[] testQuestions = allQuestions.subList(0, Math.min(10, allQuestions.size()))
                .toArray(new String[0]);

        // Before 테스트 (메타데이터 없이)
        System.out.println("=== Before 테스트 (메타데이터 없이) ===");
        for (int i = 0; i < testQuestions.length; i++) {
            String question = testQuestions[i];
            try {
                // Rate limit 방지를 위한 지연
                if (i > 0) {
                    Thread.sleep(7000); // 7초 대기
                }
                
                long startTime = System.currentTimeMillis();
                
                // 질문 분석
                QueryAnalysisResult analysis = queryAnalysisService.analyzeQuery(question);
                if (analysis == null) {
                    continue;
                }
                
                // 답변 생성 (메타데이터 비활성화, Few-shot은 유지)
                com.guideon.util.PromptTemplate.setUseMetadata(false);
                RegulationSearchResult searchResult;
                try {
                    searchResult = regulationSearchService.search(analysis);
                } finally {
                    // 원래 상태로 복원
                    com.guideon.util.PromptTemplate.setUseMetadata(true);
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
                    testResult.getMetadata().put("intent", analysis.getIntent());
                    beforeAnswerResults.getResults().add(testResult);
                    
                    System.out.println("\n질문: " + question);
                    System.out.println("  의도: " + analysis.getIntent());
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

        // After 테스트 (메타데이터 포함)
        System.out.println("\n=== After 테스트 (메타데이터 포함) ===");
        for (int i = 0; i < testQuestions.length; i++) {
            String question = testQuestions[i];
            try {
                // Rate limit 방지를 위한 지연
                if (i > 0) {
                    Thread.sleep(7000); // 7초 대기
                }
                
                long startTime = System.currentTimeMillis();
                
                // 질문 분석
                QueryAnalysisResult analysis = queryAnalysisService.analyzeQuery(question);
                if (analysis == null) {
                    continue;
                }
                
                // 답변 생성 (메타데이터 포함, Few-shot도 유지)
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
                    testResult.getMetadata().put("intent", analysis.getIntent());
                    afterAnswerResults.getResults().add(testResult);
                    
                    System.out.println("\n질문: " + question);
                    System.out.println("  의도: " + analysis.getIntent());
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
            TestResultRecorder.saveResults(beforeAnswerResults, "metadata-comparison-before.json");
        }
        if (!afterAnswerResults.getResults().isEmpty()) {
            TestResultRecorder.saveResults(afterAnswerResults, "metadata-comparison-after.json");
        }

        // 비교 리포트 생성
        if (!beforeAnswerResults.getResults().isEmpty() && !afterAnswerResults.getResults().isEmpty()) {
            Map<String, Object> answerComparison = TestResultRecorder.compareResults(beforeAnswerResults, afterAnswerResults);
            TestResultRecorder.saveComparisonReport(answerComparison, "metadata-comparison-report.json");
            
            System.out.println("\n========================================");
            System.out.println("답변 생성 비교 결과 저장 완료");
            System.out.println("========================================");
            System.out.println("- Before 결과: test-results/metadata-comparison-before.json");
            System.out.println("- After 결과: test-results/metadata-comparison-after.json");
            System.out.println("- 비교 리포트: test-results/metadata-comparison-report.json");
            System.out.println("========================================\n");
        }

        // 기본 검증
        assertFalse(beforeAnswerResults.getResults().isEmpty() || afterAnswerResults.getResults().isEmpty(), 
                   "답변 생성 테스트 결과가 있어야 합니다");
    }

    @Test
    @DisplayName("메타데이터 품질 검증 테스트")
    void testMetadataQuality() {
        System.out.println("\n========================================");
        System.out.println("메타데이터 품질 검증 테스트");
        System.out.println("========================================\n");

        // 의도별 메타데이터 검증
        String[] intents = {"기준확인", "절차설명", "가능여부", "예외상황", "계산방법", "권리의무", "정보조회"};
        for (String intent : intents) {
            IntentMetadata metadata = PromptMetadataManager.getIntentMetadata(intent);
            System.out.println("\n" + intent + " 의도 메타데이터:");
            assertNotNull(metadata, intent + " 의도 메타데이터가 null이면 안됩니다");
            assertNotNull(metadata.getGuidelines(), intent + " 의도 가이드라인이 null이면 안됩니다");
            assertFalse(metadata.getGuidelines().isEmpty(), intent + " 의도 가이드라인이 비어있으면 안됩니다");
            
            System.out.println("  설명: " + metadata.getDescription());
            System.out.println("  답변 형식: " + metadata.getAnswerFormat());
            System.out.println("  가이드라인 수: " + metadata.getGuidelines().size());
            for (int i = 0; i < metadata.getGuidelines().size(); i++) {
                System.out.println("    " + (i + 1) + ". " + metadata.getGuidelines().get(i));
            }
            System.out.println("  예상 키워드: " + metadata.getExpectedKeywords());
            System.out.println("  관련 규정 유형: " + metadata.getCommonRegulationTypes());
        }

        System.out.println("\n✓ 메타데이터 품질 검증 완료");
    }
}

