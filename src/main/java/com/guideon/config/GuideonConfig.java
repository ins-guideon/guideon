package com.guideon.config;

import com.guideon.service.*;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Guideon Service Configuration
 *
 * LangChain4j 기반 서비스들을 Spring Bean으로 등록
 */
@Configuration
public class GuideonConfig {

    /**
     * ConfigLoader Bean
     */
    @Bean
    public ConfigLoader configLoader() {
        return new ConfigLoader();
    }

    /**
     * 질문 분석 서비스 Bean
     */
    @Bean
    public QueryAnalysisService queryAnalysisService(ConfigLoader configLoader) {
        return new QueryAnalysisService(configLoader);
    }

    /**
     * EmbeddingService Bean
     */
    @Bean
    public EmbeddingService embeddingService(ConfigLoader configLoader) {
        return new EmbeddingService(configLoader);
    }

    /**
     * BM25 검색 서비스 Bean
     */
    @Bean
    public BM25SearchService bm25SearchService(ConfigLoader configLoader) throws IOException {
        if (configLoader.isHybridSearchEnabled()) {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .info("Initializing BM25SearchService for Hybrid Search");
            return new BM25SearchService(configLoader);
        } else {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .info("Hybrid Search is disabled, BM25SearchService will not be initialized");
            return null;
        }
    }

    /**
     * Hybrid 검색 서비스 Bean
     */
    @Bean
    public HybridSearchService hybridSearchService(
            ConfigLoader configLoader,
            BM25SearchService bm25SearchService,
            EmbeddingService embeddingService) {

        if (configLoader.isHybridSearchEnabled() && bm25SearchService != null) {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .info("Initializing HybridSearchService");

            InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

            return new HybridSearchService(
                bm25SearchService,
                embeddingStore,
                embeddingService,
                configLoader
            );
        } else {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .info("Hybrid Search is disabled, HybridSearchService will not be initialized");
            return null;
        }
    }

    /**
     * 규정 검색 서비스 Bean
     */
    @Bean
    public RegulationSearchService regulationSearchService(
            ConfigLoader configLoader,
            VectorStoreService vectorStoreService,
            HybridSearchService hybridSearchService) {

        RegulationSearchService service = new RegulationSearchService(configLoader, hybridSearchService);

        // 서버 시작 시 저장된 벡터 데이터 로드
        try {
            InMemoryEmbeddingStore<TextSegment> store = vectorStoreService.loadEmbeddingStore();

            // 임베딩 차원 검증 (기존 데이터와 새 모델의 차원이 다르면 초기화)
            int configuredDimension = configLoader.getEmbeddingDimension();
            if (store != null && !isEmbeddingStoreDimensionValid(store, configuredDimension)) {
                org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                    .warn("Embedding dimension mismatch detected. Expected: {}, clearing old data and creating new store.",
                        configuredDimension);
                vectorStoreService.clearAll();
                store = new InMemoryEmbeddingStore<>();
            }

            service.setEmbeddingStore(store);

            // Hybrid Search가 활성화된 경우 동일한 스토어 공유
            if (hybridSearchService != null && configLoader.isHybridSearchEnabled()) {
                hybridSearchService.setEmbeddingStore(store);
                org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                    .info("Hybrid Search enabled: Vector Store shared with HybridSearchService");
            }

        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .error("Failed to load embedding store, creating new one", e);
            InMemoryEmbeddingStore<TextSegment> newStore = new InMemoryEmbeddingStore<>();
            service.setEmbeddingStore(newStore);

            // Hybrid Search에도 동일한 스토어 설정
            if (hybridSearchService != null && configLoader.isHybridSearchEnabled()) {
                hybridSearchService.setEmbeddingStore(newStore);
            }
        }

        return service;
    }

    /**
     * 임베딩 스토어의 차원이 설정값과 일치하는지 검증
     */
    private boolean isEmbeddingStoreDimensionValid(InMemoryEmbeddingStore<TextSegment> store, int expectedDimension) {
        try {
            // 스토어가 비어있으면 유효함
            String json = store.serializeToJson();
            if (json == null || json.isEmpty() || json.equals("{}") || json.contains("\"entries\":[]")) {
                return true;
            }

            // JSON에서 차원 정보 확인
            if (json.contains("\"vector\":[") || json.contains("\"vector\": [")) {
                // 첫 번째 벡터의 길이를 확인
                int vectorStart = json.indexOf("\"vector\":[");
                if (vectorStart == -1) {
                    vectorStart = json.indexOf("\"vector\": [");
                }
                if (vectorStart != -1) {
                    int arrayStart = json.indexOf("[", vectorStart);
                    int arrayEnd = json.indexOf("]", arrayStart);
                    if (arrayStart != -1 && arrayEnd != -1) {
                        String vectorContent = json.substring(arrayStart + 1, arrayEnd);
                        int commaCount = vectorContent.split(",").length;
                        return commaCount == expectedDimension;
                    }
                }
            }

            return true; // 차원을 확인할 수 없으면 유효하다고 가정
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .warn("Failed to validate embedding dimension", e);
            return false; // 오류 발생 시 새로 생성
        }
    }
}
