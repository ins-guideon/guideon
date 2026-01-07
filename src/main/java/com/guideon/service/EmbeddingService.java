package com.guideon.service;

import com.guideon.config.GuideonProperties;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 임베딩 생성 서비스
 * 텍스트를 벡터 임베딩으로 변환
 */
public class EmbeddingService {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(GuideonProperties properties) {
        String apiKey = properties.getGemini().getApi().getKey();
        String modelName = properties.getEmbedding().getModel().getName();
        
        this.embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxRetries(3)
                .build();

        logger.info("EmbeddingService initialized with model: {}", modelName);
    }

    /**
     * 텍스트를 임베딩으로 변환
     *
     * @param text 변환할 텍스트
     * @return 임베딩 응답
     */
    public Response<Embedding> embed(String text) {
        return embeddingModel.embed(text);
    }

    /**
     * 임베딩 모델 반환
     *
     * @return EmbeddingModel
     */
    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }
}
