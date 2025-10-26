package com.guideon.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 규정 문서 업로드 요청 DTO
 */
public class UploadRequest {

    @NotBlank(message = "파일 경로는 필수입니다")
    private String filePath;

    @NotBlank(message = "규정 유형은 필수입니다")
    private String regulationType;

    public UploadRequest() {
    }

    public UploadRequest(String filePath, String regulationType) {
        this.filePath = filePath;
        this.regulationType = regulationType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRegulationType() {
        return regulationType;
    }

    public void setRegulationType(String regulationType) {
        this.regulationType = regulationType;
    }
}
