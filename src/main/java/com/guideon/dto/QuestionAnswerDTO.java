package com.guideon.dto;

import java.util.List;

/**
 * 질문 답변 결과 DTO (프론트엔드 응답용)
 */
public class QuestionAnswerDTO {
    private String answer;
    private QuestionAnalysisDTO analysis;
    private List<QuestionReferenceDTO> references;
    private double confidenceScore;

    public QuestionAnswerDTO() {
    }

    public QuestionAnswerDTO(String answer, QuestionAnalysisDTO analysis,
                           List<QuestionReferenceDTO> references, double confidenceScore) {
        this.answer = answer;
        this.analysis = analysis;
        this.references = references;
        this.confidenceScore = confidenceScore;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public QuestionAnalysisDTO getAnalysis() {
        return analysis;
    }

    public void setAnalysis(QuestionAnalysisDTO analysis) {
        this.analysis = analysis;
    }

    public List<QuestionReferenceDTO> getReferences() {
        return references;
    }

    public void setReferences(List<QuestionReferenceDTO> references) {
        this.references = references;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
