package com.guideon.model;

/**
 * 문서 메타데이터 클래스
 */
public class DocumentMetadata {
    private final String id;
    private final String fileName;
    private final String regulationType;
    private final long fileSize;
    private final String savedFileName;
    private final long uploadTimestamp;
    private final String status;

    public DocumentMetadata(String id, String fileName, String regulationType,
                            long fileSize, String savedFileName, long uploadTimestamp, String status) {
        this.id = id;
        this.fileName = fileName;
        this.regulationType = regulationType;
        this.fileSize = fileSize;
        this.savedFileName = savedFileName;
        this.uploadTimestamp = uploadTimestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRegulationType() {
        return regulationType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getSavedFileName() {
        return savedFileName;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public String getStatus() {
        return status;
    }
}
