package com.guideon.model;

import java.util.List;

/**
 * 규정 검색 결과를 담는 모델
 */
public class RegulationSearchResult {
    private String answer;                      // AI 생성 답변
    private List<RegulationReference> references; // 근거 규정 목록
    private double confidenceScore;             // 신뢰도 점수 (0.0 ~ 1.0)
    private boolean foundRelevantRegulation;    // 관련 규정 발견 여부

    public RegulationSearchResult() {}

    public RegulationSearchResult(String answer, List<RegulationReference> references,
                                 double confidenceScore, boolean foundRelevantRegulation) {
        this.answer = answer;
        this.references = references;
        this.confidenceScore = confidenceScore;
        this.foundRelevantRegulation = foundRelevantRegulation;
    }

    // Getters and Setters
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<RegulationReference> getReferences() {
        return references;
    }

    public void setReferences(List<RegulationReference> references) {
        this.references = references;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public boolean isFoundRelevantRegulation() {
        return foundRelevantRegulation;
    }

    public void setFoundRelevantRegulation(boolean foundRelevantRegulation) {
        this.foundRelevantRegulation = foundRelevantRegulation;
    }

    @Override
    public String toString() {
        return "RegulationSearchResult{" +
                "answer='" + answer + '\'' +
                ", references=" + references +
                ", confidenceScore=" + confidenceScore +
                ", foundRelevantRegulation=" + foundRelevantRegulation +
                '}';
    }
}
