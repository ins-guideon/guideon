package com.guideon.dto;

import com.guideon.model.RegulationSearchResult;

/**
 * 규정 검색 결과 응답 DTO
 */
public class SearchResponse {

    private boolean success;
    private String message;
    private RegulationSearchResult result;

    public SearchResponse() {
    }

    public SearchResponse(boolean success, String message, RegulationSearchResult result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public static SearchResponse success(RegulationSearchResult result) {
        return new SearchResponse(true, "검색이 완료되었습니다", result);
    }

    public static SearchResponse error(String message) {
        return new SearchResponse(false, message, null);
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

    public RegulationSearchResult getResult() {
        return result;
    }

    public void setResult(RegulationSearchResult result) {
        this.result = result;
    }
}
