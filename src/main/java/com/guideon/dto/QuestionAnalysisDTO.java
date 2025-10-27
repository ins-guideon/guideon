package com.guideon.dto;

import java.util.List;

/**
 * 질문 분석 정보 DTO (프론트엔드 응답용)
 */
public class QuestionAnalysisDTO {
    private List<String> keywords;
    private List<String> regulationTypes;
    private String questionIntent;

    public QuestionAnalysisDTO() {
    }

    public QuestionAnalysisDTO(List<String> keywords, List<String> regulationTypes, String questionIntent) {
        this.keywords = keywords;
        this.regulationTypes = regulationTypes;
        this.questionIntent = questionIntent;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getRegulationTypes() {
        return regulationTypes;
    }

    public void setRegulationTypes(List<String> regulationTypes) {
        this.regulationTypes = regulationTypes;
    }

    public String getQuestionIntent() {
        return questionIntent;
    }

    public void setQuestionIntent(String questionIntent) {
        this.questionIntent = questionIntent;
    }
}
