package com.guideon.dto;

public class ConfirmEmbeddingRequest {
    private String text;

    public ConfirmEmbeddingRequest() {
    }

    public ConfirmEmbeddingRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
