package com.guideon.config;

import com.guideon.service.QueryAnalysisService;
import com.guideon.service.RegulationSearchService;
import com.guideon.service.VectorStoreService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * 규정 검색 서비스 Bean
     */
    @Bean
    public RegulationSearchService regulationSearchService(ConfigLoader configLoader, VectorStoreService vectorStoreService) {
        RegulationSearchService service = new RegulationSearchService(configLoader);

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
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(GuideonConfig.class)
                .error("Failed to load embedding store, creating new one", e);
            service.setEmbeddingStore(new InMemoryEmbeddingStore<>());
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
