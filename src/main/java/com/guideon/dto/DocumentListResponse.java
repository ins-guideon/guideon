package com.guideon.dto;

import java.util.List;

/**
 * 문서 목록 응답 DTO
 */
public class DocumentListResponse {
    private List<DocumentInfo> documents;
    private int totalCount;

    public DocumentListResponse() {
    }

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

        public DocumentInfo(String id, String fileName, String regulationType,
                          long fileSize, long uploadTimestamp, String status) {
            this.id = id;
            this.fileName = fileName;
            this.regulationType = regulationType;
            this.fileSize = fileSize;
            this.uploadTimestamp = uploadTimestamp;
            this.status = status;
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
