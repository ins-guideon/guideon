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
        InMemoryEmbeddingStore<TextSegment> store = vectorStoreService.loadEmbeddingStore();
        service.setEmbeddingStore(store);

        return service;
    }
}
