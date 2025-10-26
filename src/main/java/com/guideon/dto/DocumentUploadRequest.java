package com.guideon.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 문서 업로드 요청 DTO
 */
public class DocumentUploadRequest {

    @NotBlank(message = "규정 유형은 필수입니다")
    private String regulationType;

    public DocumentUploadRequest() {
    }

    public DocumentUploadRequest(String regulationType) {
        this.regulationType = regulationType;
    }

    public String getRegulationType() {
        return regulationType;
    }

    public void setRegulationType(String regulationType) {
        this.regulationType = regulationType;
    }
}
