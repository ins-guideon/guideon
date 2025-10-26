package com.guideon.model;

/**
 * 규정 참조 정보 (근거 조항)
 */
public class RegulationReference {
    private String documentName;    // 규정 문서명
    private String articleNumber;   // 조항 번호
    private String content;         // 조항 내용
    private int pageNumber;         // 페이지 번호
    private double relevanceScore;  // 관련성 점수

    public RegulationReference() {}

    public RegulationReference(String documentName, String articleNumber,
                              String content, int pageNumber, double relevanceScore) {
        this.documentName = documentName;
        this.articleNumber = articleNumber;
        this.content = content;
        this.pageNumber = pageNumber;
        this.relevanceScore = relevanceScore;
    }

    // Getters and Setters
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    @Override
    public String toString() {
        return "RegulationReference{" +
                "documentName='" + documentName + '\'' +
                ", articleNumber='" + articleNumber + '\'' +
                ", content='" + content + '\'' +
                ", pageNumber=" + pageNumber +
                ", relevanceScore=" + relevanceScore +
                '}';
    }
}
