package com.guideon.model.prompt;

import java.util.List;

/**
 * 질문 의도별 메타데이터
 * 프롬프트 엔지니어링에 사용되는 질문 의도 정보
 */
public class IntentMetadata {
    private String intent;              // 의도명
    private String description;         // 의도 설명
    private String answerFormat;        // 답변 형식 가이드
    private List<String> guidelines;     // 답변 작성 지침
    private List<String> expectedKeywords; // 예상 키워드
    private List<String> commonRegulationTypes; // 관련 규정 유형

    public IntentMetadata() {}

    public IntentMetadata(String intent, String description, String answerFormat,
                         List<String> guidelines, List<String> expectedKeywords,
                         List<String> commonRegulationTypes) {
        this.intent = intent;
        this.description = description;
        this.answerFormat = answerFormat;
        this.guidelines = guidelines;
        this.expectedKeywords = expectedKeywords;
        this.commonRegulationTypes = commonRegulationTypes;
    }

    // Getters and Setters
    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnswerFormat() {
        return answerFormat;
    }

    public void setAnswerFormat(String answerFormat) {
        this.answerFormat = answerFormat;
    }

    public List<String> getGuidelines() {
        return guidelines;
    }

    public void setGuidelines(List<String> guidelines) {
        this.guidelines = guidelines;
    }

    public List<String> getExpectedKeywords() {
        return expectedKeywords;
    }

    public void setExpectedKeywords(List<String> expectedKeywords) {
        this.expectedKeywords = expectedKeywords;
    }

    public List<String> getCommonRegulationTypes() {
        return commonRegulationTypes;
    }

    public void setCommonRegulationTypes(List<String> commonRegulationTypes) {
        this.commonRegulationTypes = commonRegulationTypes;
    }

    @Override
    public String toString() {
        return "IntentMetadata{" +
                "intent='" + intent + '\'' +
                ", description='" + description + '\'' +
                ", answerFormat='" + answerFormat + '\'' +
                '}';
    }
}

