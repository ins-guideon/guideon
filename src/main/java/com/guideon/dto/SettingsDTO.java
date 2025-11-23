package com.guideon.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 애플리케이션 설정 DTO
 */
public class SettingsDTO {

    private String apiKey;

    @NotBlank(message = "검색 모델은 필수입니다")
    private String searchModel;

    @NotBlank(message = "임베딩 모델은 필수입니다")
    private String embeddingModel;

    @NotNull(message = "청크 크기는 필수입니다")
    @Min(value = 100, message = "청크 크기는 최소 100이어야 합니다")
    @Max(value = 2000, message = "청크 크기는 최대 2000이어야 합니다")
    private Integer chunkSize;

    @NotNull(message = "청크 오버랩은 필수입니다")
    @Min(value = 0, message = "청크 오버랩은 최소 0이어야 합니다")
    @Max(value = 500, message = "청크 오버랩은 최대 500이어야 합니다")
    private Integer chunkOverlap;

    public SettingsDTO() {
    }

    public SettingsDTO(String apiKey, String searchModel, String embeddingModel, Integer chunkSize,
            Integer chunkOverlap) {
        this.apiKey = apiKey;
        this.searchModel = searchModel;
        this.embeddingModel = embeddingModel;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSearchModel() {
        return searchModel;
    }

    public void setSearchModel(String searchModel) {
        this.searchModel = searchModel;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Integer getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(Integer chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }
}
