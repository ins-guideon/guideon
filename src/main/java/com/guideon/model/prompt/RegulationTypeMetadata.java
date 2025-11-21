package com.guideon.model.prompt;

import java.util.List;

/**
 * 규정 유형별 메타데이터
 * 프롬프트 엔지니어링에 사용되는 규정 유형 정보
 */
public class RegulationTypeMetadata {
    private String regulationType;      // 규정 유형명
    private String description;        // 규정 설명
    private List<String> keywords;      // 주요 키워드
    private List<String> relatedIntents; // 관련 질문 의도
    private String answerFormatGuide;   // 답변 형식 가이드
    private int priority;               // 우선순위

    public RegulationTypeMetadata() {}

    public RegulationTypeMetadata(String regulationType, String description,
                                 List<String> keywords, List<String> relatedIntents,
                                 String answerFormatGuide, int priority) {
        this.regulationType = regulationType;
        this.description = description;
        this.keywords = keywords;
        this.relatedIntents = relatedIntents;
        this.answerFormatGuide = answerFormatGuide;
        this.priority = priority;
    }

    // Getters and Setters
    public String getRegulationType() {
        return regulationType;
    }

    public void setRegulationType(String regulationType) {
        this.regulationType = regulationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getRelatedIntents() {
        return relatedIntents;
    }

    public void setRelatedIntents(List<String> relatedIntents) {
        this.relatedIntents = relatedIntents;
    }

    public String getAnswerFormatGuide() {
        return answerFormatGuide;
    }

    public void setAnswerFormatGuide(String answerFormatGuide) {
        this.answerFormatGuide = answerFormatGuide;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "RegulationTypeMetadata{" +
                "regulationType='" + regulationType + '\'' +
                ", description='" + description + '\'' +
                ", keywords=" + keywords +
                ", relatedIntents=" + relatedIntents +
                ", priority=" + priority +
                '}';
    }
}

