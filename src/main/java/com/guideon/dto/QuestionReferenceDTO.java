package com.guideon.dto;

/**
 * 규정 참조 정보 DTO (프론트엔드 응답용)
 */
public class QuestionReferenceDTO {
    private String documentName;
    private String content;
    private double relevanceScore;

    public QuestionReferenceDTO() {
    }

    public QuestionReferenceDTO(String documentName, String content, double relevanceScore) {
        this.documentName = documentName;
        this.content = content;
        this.relevanceScore = relevanceScore;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
}
