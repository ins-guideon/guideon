package com.guideon.model;

import java.util.List;

/**
 * 자연어 질문 분석 결과를 담는 모델
 */
public class QueryAnalysisResult {
    private String originalQuery;          // 원본 질문
    private List<String> keywords;         // 추출된 키워드
    private List<String> regulationTypes;  // 관련 규정 유형
    private String intent;                 // 질문 의도 (정보조회, 절차안내, 기준확인 등)
    private String searchQuery;            // 변환된 검색 쿼리

    public QueryAnalysisResult() {}

    public QueryAnalysisResult(String originalQuery, List<String> keywords,
                              List<String> regulationTypes, String intent, String searchQuery) {
        this.originalQuery = originalQuery;
        this.keywords = keywords;
        this.regulationTypes = regulationTypes;
        this.intent = intent;
        this.searchQuery = searchQuery;
    }

    // Getters and Setters
    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
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

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    public String toString() {
        return "QueryAnalysisResult{" +
                "originalQuery='" + originalQuery + '\'' +
                ", keywords=" + keywords +
                ", regulationTypes=" + regulationTypes +
                ", intent='" + intent + '\'' +
                ", searchQuery='" + searchQuery + '\'' +
                '}';
    }
}
