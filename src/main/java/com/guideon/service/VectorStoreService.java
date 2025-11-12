package com.guideon.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 벡터 스토어 영속성 서비스
 * InMemoryEmbeddingStore의 데이터를 파일시스템에 저장하고 로드
 */
@Service
public class VectorStoreService {
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final String dataDir;
    private final String embeddingStoreFile;

    public VectorStoreService() {
        this.dataDir = System.getProperty("user.home") + "/guideon/data";
        this.embeddingStoreFile = dataDir + "/embedding_store.json";

        // 데이터 디렉토리 생성
        try {
            Files.createDirectories(Paths.get(dataDir));
            logger.info("Vector store data directory initialized: {}", dataDir);
        } catch (Exception e) {
            logger.error("Failed to create data directory", e);
        }
    }

    /**
     * EmbeddingStore를 파일에 저장
     */
    public void saveEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
        logger.info("Saving embedding store to file system...");

        try {
            if (embeddingStore instanceof InMemoryEmbeddingStore) {
                InMemoryEmbeddingStore<TextSegment> inMemoryStore = (InMemoryEmbeddingStore<TextSegment>) embeddingStore;

                // InMemoryEmbeddingStore를 JSON으로 직렬화하여 저장
                String json = inMemoryStore.serializeToJson();
                Files.writeString(Paths.get(embeddingStoreFile), json);

                logger.info("Embedding store saved successfully to: {}", embeddingStoreFile);
            } else {
                logger.warn("Unsupported embedding store type: {}", embeddingStore.getClass().getName());
            }
        } catch (Exception e) {
            logger.error("Failed to save embedding store", e);
            throw new RuntimeException("벡터 스토어 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일에서 EmbeddingStore 로드
     */
    public InMemoryEmbeddingStore<TextSegment> loadEmbeddingStore() {
        logger.info("Loading embedding store from file system...");

        try {
            Path path = Paths.get(embeddingStoreFile);

            if (!Files.exists(path)) {
                logger.info("No existing embedding store found, creating new one");
                return new InMemoryEmbeddingStore<>();
            }

            // JSON에서 InMemoryEmbeddingStore 역직렬화
            String json = Files.readString(path);
            InMemoryEmbeddingStore<TextSegment> store = InMemoryEmbeddingStore.fromJson(json);

            logger.info("Embedding store loaded successfully from: {}", embeddingStoreFile);
            return store;

        } catch (Exception e) {
            logger.error("Failed to load embedding store, creating new one", e);
            return new InMemoryEmbeddingStore<>();
        }
    }

    /**
     * 모든 데이터 삭제
     */
    public void clearAll() {
        logger.info("Clearing all vector store data...");

        try {
            Files.deleteIfExists(Paths.get(embeddingStoreFile));
            logger.info("All vector store data cleared");
        } catch (Exception e) {
            logger.error("Failed to clear data", e);
        }
    }

    /**
     * 데이터 파일이 존재하는지 확인
     */
    public boolean hasPersistedData() {
        return Files.exists(Paths.get(embeddingStoreFile));
    }

    /**
     * 저장된 데이터 통계
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Path embeddingPath = Paths.get(embeddingStoreFile);

            stats.put("embeddingStoreExists", Files.exists(embeddingPath));

            if (Files.exists(embeddingPath)) {
                stats.put("embeddingStoreSize", Files.size(embeddingPath));
            }

        } catch (Exception e) {
            logger.error("Failed to get stats", e);
        }

        return stats;
    }
}
