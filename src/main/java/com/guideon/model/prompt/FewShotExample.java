package com.guideon.model.prompt;

import java.util.List;

/**
 * Few-shot 예제 메타데이터
 * 프롬프트에 포함될 예제 정보
 */
public class FewShotExample {
    private String id;                  // 예제 ID
    private String question;            // 질문
    private String answer;              // 답변 (답변 생성용)
    private String analysisResult;      // 분석 결과 (질문 분석용)
    private String sourceRegulation;    // 출처 규정
    private String intent;              // 질문 의도
    private List<String> regulationTypes; // 관련 규정 유형
    private double confidenceScore;     // 신뢰도 점수 (0.0 ~ 1.0)
    private int usageFrequency;        // 사용 빈도
    private List<String> tags;         // 태그
    private String exampleType;         // 예제 유형: "query_analysis" 또는 "answer_generation"

    public FewShotExample() {}

    public FewShotExample(String id, String question, String answer, String analysisResult,
                         String sourceRegulation, String intent, List<String> regulationTypes,
                         double confidenceScore, int usageFrequency, List<String> tags,
                         String exampleType) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.analysisResult = analysisResult;
        this.sourceRegulation = sourceRegulation;
        this.intent = intent;
        this.regulationTypes = regulationTypes;
        this.confidenceScore = confidenceScore;
        this.usageFrequency = usageFrequency;
        this.tags = tags;
        this.exampleType = exampleType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public String getSourceRegulation() {
        return sourceRegulation;
    }

    public void setSourceRegulation(String sourceRegulation) {
        this.sourceRegulation = sourceRegulation;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public List<String> getRegulationTypes() {
        return regulationTypes;
    }

    public void setRegulationTypes(List<String> regulationTypes) {
        this.regulationTypes = regulationTypes;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public int getUsageFrequency() {
        return usageFrequency;
    }

    public void setUsageFrequency(int usageFrequency) {
        this.usageFrequency = usageFrequency;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getExampleType() {
        return exampleType;
    }

    public void setExampleType(String exampleType) {
        this.exampleType = exampleType;
    }

    @Override
    public String toString() {
        return "FewShotExample{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                ", intent='" + intent + '\'' +
                ", exampleType='" + exampleType + '\'' +
                '}';
    }
}

