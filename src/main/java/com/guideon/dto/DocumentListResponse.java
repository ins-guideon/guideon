package com.guideon.dto;

import com.guideon.model.DocumentMetadata;

import java.util.List;

/**
 * 문서 목록 응답 DTO
 */
public class DocumentListResponse {
    private List<DocumentInfo> documents;
    private int totalCount;

    public DocumentListResponse(List<DocumentInfo> documents, int totalCount) {
        this.documents = documents;
        this.totalCount = totalCount;
    }

    public List<DocumentInfo> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentInfo> documents) {
        this.documents = documents;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 문서 정보
     */
    public static class DocumentInfo {
        private String id;
        private String fileName;
        private String regulationType;
        private long fileSize;
        private long uploadTimestamp;
        private String status;

        public DocumentInfo() {
        }

        public DocumentInfo(DocumentMetadata document) {
            this.id = document.getId();
            this.fileName = document.getFileName();
            this.regulationType = document.getRegulationType();
            this.fileSize = document.getFileSize() != null ? document.getFileSize() : 0L;
            this.uploadTimestamp = document.getUploadTime() != null ? document.getUploadTime().toEpochMilli() : 0L;
            this.status = document.getStatus();
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
    }
}
