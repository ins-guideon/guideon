package com.guideon.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.guideon.model.QueryAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 테스트 결과 기록 및 비교 유틸리티 클래스
 */
public class TestResultRecorder {
    private static final Logger logger = LoggerFactory.getLogger(TestResultRecorder.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    private static final String TEST_RESULTS_DIR = "test-results";

    /**
     * 테스트 결과를 저장하는 내부 클래스
     */
    public static class TestResult {
        private String question;
        private QueryAnalysisResult analysisResult;
        private String answer;
        private long responseTimeMs;
        private Map<String, Object> metadata;
        private String timestamp;

        public TestResult() {
            this.metadata = new HashMap<>();
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // Getters and Setters
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public QueryAnalysisResult getAnalysisResult() {
            return analysisResult;
        }

        public void setAnalysisResult(QueryAnalysisResult analysisResult) {
            this.analysisResult = analysisResult;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public void setResponseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 테스트 결과 세트를 저장하는 클래스
     */
    public static class TestResultSet {
        private String testName;
        private String version; // "before" or "after"
        private List<TestResult> results;
        private Map<String, Object> summary;
        private String timestamp;

        public TestResultSet() {
            this.results = new ArrayList<>();
            this.summary = new HashMap<>();
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // Getters and Setters
        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<TestResult> getResults() {
            return results;
        }

        public void setResults(List<TestResult> results) {
            this.results = results;
        }

        public Map<String, Object> getSummary() {
            return summary;
        }

        public void setSummary(Map<String, Object> summary) {
            this.summary = summary;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 테스트 결과를 JSON 파일로 저장
     */
    public static void saveResults(TestResultSet resultSet, String filename) {
        try {
            // 디렉토리 생성
            Files.createDirectories(Paths.get(TEST_RESULTS_DIR));

            // 통계 계산
            calculateSummary(resultSet);

            // 파일 저장
            File file = new File(TEST_RESULTS_DIR, filename);
            objectMapper.writeValue(file, resultSet);

            logger.info("테스트 결과 저장 완료: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("테스트 결과 저장 실패", e);
            throw new RuntimeException("테스트 결과 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 결과 세트의 통계 정보 계산
     */
    private static void calculateSummary(TestResultSet resultSet) {
        Map<String, Object> summary = new HashMap<>();
        List<TestResult> results = resultSet.getResults();

        if (results.isEmpty()) {
            resultSet.setSummary(summary);
            return;
        }

        // 평균 응답 시간
        double avgResponseTime = results.stream()
                .mapToLong(TestResult::getResponseTimeMs)
                .average()
                .orElse(0.0);
        summary.put("averageResponseTimeMs", avgResponseTime);

        // 총 테스트 수
        summary.put("totalTests", results.size());

        // 의도 분류 통계
        Map<String, Long> intentCounts = new HashMap<>();
        for (TestResult result : results) {
            if (result.getAnalysisResult() != null && result.getAnalysisResult().getIntent() != null) {
                String intent = result.getAnalysisResult().getIntent();
                intentCounts.put(intent, intentCounts.getOrDefault(intent, 0L) + 1);
            }
        }
        summary.put("intentDistribution", intentCounts);

        // 규정 유형 통계
        Map<String, Long> regulationTypeCounts = new HashMap<>();
        for (TestResult result : results) {
            if (result.getAnalysisResult() != null && result.getAnalysisResult().getRegulationTypes() != null) {
                for (String type : result.getAnalysisResult().getRegulationTypes()) {
                    regulationTypeCounts.put(type, regulationTypeCounts.getOrDefault(type, 0L) + 1);
                }
            }
        }
        summary.put("regulationTypeDistribution", regulationTypeCounts);

        // 키워드 추출 통계
        int totalKeywords = 0;
        for (TestResult result : results) {
            if (result.getAnalysisResult() != null && result.getAnalysisResult().getKeywords() != null) {
                totalKeywords += result.getAnalysisResult().getKeywords().size();
            }
        }
        double avgKeywords = results.size() > 0 ? (double) totalKeywords / results.size() : 0.0;
        summary.put("averageKeywordsPerQuery", avgKeywords);

        // 답변 생성 통계 (답변이 있는 경우)
        List<TestResult> resultsWithAnswers = results.stream()
                .filter(r -> r.getAnswer() != null && !r.getAnswer().isEmpty())
                .collect(java.util.stream.Collectors.toList());
        
        if (!resultsWithAnswers.isEmpty()) {
            // 평균 답변 길이
            double avgAnswerLength = resultsWithAnswers.stream()
                    .mapToInt(r -> r.getAnswer().length())
                    .average()
                    .orElse(0.0);
            summary.put("averageAnswerLength", avgAnswerLength);
            
            // 평균 답변 품질 점수
            if (resultsWithAnswers.stream().anyMatch(r -> r.getMetadata().containsKey("qualityScore"))) {
                double avgQualityScore = resultsWithAnswers.stream()
                        .filter(r -> r.getMetadata().containsKey("qualityScore"))
                        .mapToDouble(r -> ((Number) r.getMetadata().get("qualityScore")).doubleValue())
                        .average()
                        .orElse(0.0);
                summary.put("averageQualityScore", avgQualityScore);
            }
            
            // 평균 조항 인용 개수
            if (resultsWithAnswers.stream().anyMatch(r -> r.getMetadata().containsKey("articleReferences"))) {
                double avgArticleRefs = resultsWithAnswers.stream()
                        .filter(r -> r.getMetadata().containsKey("articleReferences"))
                        .mapToInt(r -> ((Number) r.getMetadata().get("articleReferences")).intValue())
                        .average()
                        .orElse(0.0);
                summary.put("averageArticleReferences", avgArticleRefs);
            }
            
            summary.put("totalAnswersGenerated", resultsWithAnswers.size());
        }

        resultSet.setSummary(summary);
    }

    /**
     * Before/After 결과를 비교하여 리포트 생성
     */
    public static Map<String, Object> compareResults(TestResultSet before, TestResultSet after) {
        Map<String, Object> comparison = new HashMap<>();

        // 기본 정보
        comparison.put("beforeVersion", before.getVersion());
        comparison.put("afterVersion", after.getVersion());
        comparison.put("testName", before.getTestName());
        comparison.put("comparisonTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 응답 시간 비교
        double beforeAvgTime = (Double) before.getSummary().getOrDefault("averageResponseTimeMs", 0.0);
        double afterAvgTime = (Double) after.getSummary().getOrDefault("averageResponseTimeMs", 0.0);
        double timeImprovement = beforeAvgTime > 0 ? ((beforeAvgTime - afterAvgTime) / beforeAvgTime) * 100 : 0.0;
        
        Map<String, Object> responseTimeComparison = new HashMap<>();
        responseTimeComparison.put("before", beforeAvgTime);
        responseTimeComparison.put("after", afterAvgTime);
        responseTimeComparison.put("improvementPercent", timeImprovement);
        comparison.put("responseTime", responseTimeComparison);

        // 질문별 상세 비교
        List<Map<String, Object>> questionComparisons = new ArrayList<>();
        int minSize = Math.min(before.getResults().size(), after.getResults().size());
        
        for (int i = 0; i < minSize; i++) {
            TestResult beforeResult = before.getResults().get(i);
            TestResult afterResult = after.getResults().get(i);
            
            Map<String, Object> questionComparison = new HashMap<>();
            questionComparison.put("question", beforeResult.getQuestion());
            
            // 키워드 비교
            if (beforeResult.getAnalysisResult() != null && afterResult.getAnalysisResult() != null) {
                List<String> beforeKeywords = beforeResult.getAnalysisResult().getKeywords();
                List<String> afterKeywords = afterResult.getAnalysisResult().getKeywords();
                questionComparison.put("keywords", Map.of(
                    "before", beforeKeywords != null ? beforeKeywords : Collections.emptyList(),
                    "after", afterKeywords != null ? afterKeywords : Collections.emptyList()
                ));
                
                // 규정 유형 비교
                List<String> beforeTypes = beforeResult.getAnalysisResult().getRegulationTypes();
                List<String> afterTypes = afterResult.getAnalysisResult().getRegulationTypes();
                questionComparison.put("regulationTypes", Map.of(
                    "before", beforeTypes != null ? beforeTypes : Collections.emptyList(),
                    "after", afterTypes != null ? afterTypes : Collections.emptyList()
                ));
                
                // 의도 비교
                questionComparison.put("intent", Map.of(
                    "before", beforeResult.getAnalysisResult().getIntent() != null ? beforeResult.getAnalysisResult().getIntent() : "",
                    "after", afterResult.getAnalysisResult().getIntent() != null ? afterResult.getAnalysisResult().getIntent() : ""
                ));
                
                // 검색 쿼리 비교
                questionComparison.put("searchQuery", Map.of(
                    "before", beforeResult.getAnalysisResult().getSearchQuery() != null ? beforeResult.getAnalysisResult().getSearchQuery() : "",
                    "after", afterResult.getAnalysisResult().getSearchQuery() != null ? afterResult.getAnalysisResult().getSearchQuery() : ""
                ));
            }
            
            // 응답 시간 비교
            questionComparison.put("responseTimeMs", Map.of(
                "before", beforeResult.getResponseTimeMs(),
                "after", afterResult.getResponseTimeMs()
            ));
            
            // 답변 생성 비교 (답변이 있는 경우)
            if (beforeResult.getAnswer() != null || afterResult.getAnswer() != null) {
                Map<String, Object> answerComparison = new HashMap<>();
                
                // 답변 텍스트 비교
                answerComparison.put("before", beforeResult.getAnswer() != null ? beforeResult.getAnswer() : "");
                answerComparison.put("after", afterResult.getAnswer() != null ? afterResult.getAnswer() : "");
                
                // 답변 길이 비교
                int beforeLength = beforeResult.getAnswer() != null ? beforeResult.getAnswer().length() : 0;
                int afterLength = afterResult.getAnswer() != null ? afterResult.getAnswer().length() : 0;
                answerComparison.put("length", Map.of(
                    "before", beforeLength,
                    "after", afterLength
                ));
                
                // 품질 점수 비교
                if (beforeResult.getMetadata().containsKey("qualityScore") || 
                    afterResult.getMetadata().containsKey("qualityScore")) {
                    double beforeScore = beforeResult.getMetadata().containsKey("qualityScore") 
                        ? ((Number) beforeResult.getMetadata().get("qualityScore")).doubleValue() : 0.0;
                    double afterScore = afterResult.getMetadata().containsKey("qualityScore")
                        ? ((Number) afterResult.getMetadata().get("qualityScore")).doubleValue() : 0.0;
                    answerComparison.put("qualityScore", Map.of(
                        "before", beforeScore,
                        "after", afterScore,
                        "improvement", afterScore - beforeScore
                    ));
                }
                
                // 조항 인용 개수 비교
                if (beforeResult.getMetadata().containsKey("articleReferences") || 
                    afterResult.getMetadata().containsKey("articleReferences")) {
                    int beforeRefs = beforeResult.getMetadata().containsKey("articleReferences")
                        ? ((Number) beforeResult.getMetadata().get("articleReferences")).intValue() : 0;
                    int afterRefs = afterResult.getMetadata().containsKey("articleReferences")
                        ? ((Number) afterResult.getMetadata().get("articleReferences")).intValue() : 0;
                    answerComparison.put("articleReferences", Map.of(
                        "before", beforeRefs,
                        "after", afterRefs,
                        "improvement", afterRefs - beforeRefs
                    ));
                }
                
                questionComparison.put("answer", answerComparison);
            }
            
            questionComparisons.add(questionComparison);
        }
        
        comparison.put("questionComparisons", questionComparisons);
        
        // 답변 생성 통계 비교
        if (before.getSummary().containsKey("totalAnswersGenerated") || 
            after.getSummary().containsKey("totalAnswersGenerated")) {
            Map<String, Object> answerStatsComparison = new HashMap<>();
            
            // 평균 답변 길이 비교
            if (before.getSummary().containsKey("averageAnswerLength") || 
                after.getSummary().containsKey("averageAnswerLength")) {
                double beforeAvgLength = (Double) before.getSummary().getOrDefault("averageAnswerLength", 0.0);
                double afterAvgLength = (Double) after.getSummary().getOrDefault("averageAnswerLength", 0.0);
                answerStatsComparison.put("averageAnswerLength", Map.of(
                    "before", beforeAvgLength,
                    "after", afterAvgLength,
                    "improvementPercent", beforeAvgLength > 0 ? ((afterAvgLength - beforeAvgLength) / beforeAvgLength) * 100 : 0.0
                ));
            }
            
            // 평균 품질 점수 비교
            if (before.getSummary().containsKey("averageQualityScore") || 
                after.getSummary().containsKey("averageQualityScore")) {
                double beforeAvgScore = (Double) before.getSummary().getOrDefault("averageQualityScore", 0.0);
                double afterAvgScore = (Double) after.getSummary().getOrDefault("averageQualityScore", 0.0);
                answerStatsComparison.put("averageQualityScore", Map.of(
                    "before", beforeAvgScore,
                    "after", afterAvgScore,
                    "improvement", afterAvgScore - beforeAvgScore
                ));
            }
            
            // 평균 조항 인용 개수 비교
            if (before.getSummary().containsKey("averageArticleReferences") || 
                after.getSummary().containsKey("averageArticleReferences")) {
                double beforeAvgRefs = (Double) before.getSummary().getOrDefault("averageArticleReferences", 0.0);
                double afterAvgRefs = (Double) after.getSummary().getOrDefault("averageArticleReferences", 0.0);
                answerStatsComparison.put("averageArticleReferences", Map.of(
                    "before", beforeAvgRefs,
                    "after", afterAvgRefs,
                    "improvement", afterAvgRefs - beforeAvgRefs
                ));
            }
            
            comparison.put("answerGenerationComparison", answerStatsComparison);
        }

        // 통계 비교
        Map<String, Object> summaryComparison = new HashMap<>();
        summaryComparison.put("before", before.getSummary());
        summaryComparison.put("after", after.getSummary());
        comparison.put("summaryComparison", summaryComparison);

        return comparison;
    }

    /**
     * 비교 결과를 JSON 파일로 저장
     */
    public static void saveComparisonReport(Map<String, Object> comparison, String filename) {
        try {
            Files.createDirectories(Paths.get(TEST_RESULTS_DIR));
            File file = new File(TEST_RESULTS_DIR, filename);
            objectMapper.writeValue(file, comparison);
            logger.info("비교 리포트 저장 완료: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("비교 리포트 저장 실패", e);
            throw new RuntimeException("비교 리포트 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 저장된 결과 파일 로드
     */
    public static TestResultSet loadResults(String filename) {
        try {
            File file = new File(TEST_RESULTS_DIR, filename);
            return objectMapper.readValue(file, TestResultSet.class);
        } catch (IOException e) {
            logger.error("테스트 결과 로드 실패", e);
            throw new RuntimeException("테스트 결과 로드 실패: " + e.getMessage(), e);
        }
    }
}

