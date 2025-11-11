package com.guideon.dto;

import com.guideon.model.DocumentMetadata;

/**
 * 문서 업로드 응답 DTO
 */
public class DocumentUploadResponse {
    private String id;
    private String fileName;
    private String regulationType;
    private long fileSize;
    private long uploadTimestamp;
    private String status;
    private String message;

    public DocumentUploadResponse() {
    }

    public DocumentUploadResponse(String id, String fileName, String regulationType,
                                long fileSize, long uploadTimestamp, String status, String message) {
        this.id = id;
        this.fileName = fileName;
        this.regulationType = regulationType;
        this.fileSize = fileSize;
        this.uploadTimestamp = uploadTimestamp;
        this.status = status;
        this.message = message;
    }

    public DocumentUploadResponse(DocumentMetadata metadata, String message) {
        this.id = metadata.getId();
        this.fileName = metadata.getFileName();
        this.regulationType = metadata.getRegulationType();
        this.fileSize = metadata.getFileSize();
        this.uploadTimestamp = metadata.getUploadTimestamp();
        this.status = metadata.getStatus();
        this.message = message;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
