package com.guideon.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 사용자 질문 요청 DTO
 */
public class QuestionRequest {

    @NotBlank(message = "질문은 필수입니다")
    private String question;

    public QuestionRequest() {
    }

    public QuestionRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
