package com.guideon.dto;

import com.guideon.model.QueryAnalysisResult;

/**
 * 질문 분석 결과 응답 DTO
 */
public class AnalysisResponse {

    private boolean success;
    private String message;
    private QueryAnalysisResult analysis;

    public AnalysisResponse() {
    }

    public AnalysisResponse(boolean success, String message, QueryAnalysisResult analysis) {
        this.success = success;
        this.message = message;
        this.analysis = analysis;
    }

    public static AnalysisResponse success(QueryAnalysisResult analysis) {
        return new AnalysisResponse(true, "질문 분석이 완료되었습니다", analysis);
    }

    public static AnalysisResponse error(String message) {
        return new AnalysisResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QueryAnalysisResult getAnalysis() {
        return analysis;
    }

    public void setAnalysis(QueryAnalysisResult analysis) {
        this.analysis = analysis;
    }
}
