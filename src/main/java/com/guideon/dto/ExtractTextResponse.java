package com.guideon.dto;

public class ExtractTextResponse {
    private String text;

    public ExtractTextResponse() {
    }

    public ExtractTextResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
