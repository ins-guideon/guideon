package com.guideon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "document_metadata", indexes = {
        @Index(name = "idx_documents_regulation_type", columnList = "regulationType"),
        @Index(name = "idx_documents_upload_time", columnList = "uploadTime"),
        @Index(name = "idx_documents_uploader", columnList = "uploader_id")
})
public class DocumentMetadata {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id; // 업로드 시 생성된 UUID 문자열

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String regulationType;

    @Column(nullable = false)
    private Instant uploadTime;

    @Column(columnDefinition = "CLOB")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = true)
    private UserAccount uploader;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 50)
    private String status = "indexed";

    @Column(nullable = false, length = 260)
    private String storageFileName;

    public DocumentMetadata() {
    }

    public DocumentMetadata(String id, String fileName, String regulationType, Instant uploadTime,
            String content, UserAccount uploader, Long fileSize, String storageFileName) {
        this.id = id;
        this.fileName = fileName;
        this.regulationType = regulationType;
        this.uploadTime = uploadTime;
        this.content = content;
        this.uploader = uploader;
        this.fileSize = fileSize;
        this.storageFileName = storageFileName;
    }

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

    public Instant getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Instant uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserAccount getUploader() {
        return uploader;
    }

    public void setUploader(UserAccount uploader) {
        this.uploader = uploader;
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

    public String getStorageFileName() {
        return storageFileName;
    }

    public void setStorageFileName(String storageFileName) {
        this.storageFileName = storageFileName;
    }
}
