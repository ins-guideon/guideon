package com.guideon.dto;

public class ExtractTextResponse {
    private String uploadId;
    private String text;

    public ExtractTextResponse() {
    }

    public ExtractTextResponse(String uploadId, String text) {
        this.uploadId = uploadId;
        this.text = text;
    }

    public String getUploadId() {
        return uploadId;
    }

    public String getText() {
        return text;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public void setText(String text) {
        this.text = text;
    }
}
