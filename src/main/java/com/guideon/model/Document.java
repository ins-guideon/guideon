package com.guideon.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_regulation_type", columnList = "regulationType"),
    @Index(name = "idx_documents_upload_time", columnList = "uploadTime"),
    @Index(name = "idx_documents_uploader", columnList = "uploader_id")
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String regulationType;

    @Column(nullable = false)
    private Instant uploadTime;

    @Column(columnDefinition = "CLOB")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private UserAccount uploader;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 50)
    private String status = "indexed";

    public Document() {}

    public Document(String fileName, String regulationType, Instant uploadTime, 
                   String content, UserAccount uploader, Long fileSize) {
        this.fileName = fileName;
        this.regulationType = regulationType;
        this.uploadTime = uploadTime;
        this.content = content;
        this.uploader = uploader;
        this.fileSize = fileSize;
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
}

