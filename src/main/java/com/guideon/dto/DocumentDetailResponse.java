package com.guideon.dto;

/**
 * 문서 상세 정보 응답 DTO
 */
public class DocumentDetailResponse {
    private Long id;
    private String fileName;
    private String regulationType;
    private Long uploadTime;
    private String content;
    private String uploaderName;
    private Long fileSize;
    private String status;

    public DocumentDetailResponse() {
    }

    public DocumentDetailResponse(Long id, String fileName, String regulationType,
                                 Long uploadTime, String content, String uploaderName,
                                 Long fileSize, String status) {
        this.id = id;
        this.fileName = fileName;
        this.regulationType = regulationType;
        this.uploadTime = uploadTime;
        this.content = content;
        this.uploaderName = uploaderName;
        this.fileSize = fileSize;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRegulationType() {
        return regulationType;
    }

    public void setRegulationType(String regulationType) {
        this.regulationType = regulationType;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

