package com.guideon.tool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.guideon.config.GuideonProperties;
import com.guideon.service.BM25SearchService;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * BM25 인덱스 재구축 도구
 * Embedding Store의 모든 세그먼트를 BM25 인덱스에 재색인
 * Spring Boot 환경에서 실행되어 설정을 자동으로 로드합니다.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.guideon")
@Profile("tool")
public class ReindexBM25Tool implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ReindexBM25Tool.class);

    private final BM25SearchService bm25Service;
    private final GuideonProperties properties;

    public ReindexBM25Tool(BM25SearchService bm25Service, GuideonProperties properties) {
        this.bm25Service = bm25Service;
        this.properties = properties;
    }

    public static void main(String[] args) {
        SpringApplication.run(ReindexBM25Tool.class, args);
    }

    @Override
    public void run(String... args) {
        logger.info("=".repeat(80));
        logger.info("BM25 Index Rebuild Tool - Spring Boot Version");
        logger.info("=".repeat(80));

        try {
            // Embedding Store 경로
            String embeddingStorePath = properties.getVectorstore().getPersistence().getDataDir() + "/embedding_store.json";
            Path path = Paths.get(embeddingStorePath);

            if (!Files.exists(path)) {
                logger.error("✗ Embedding store not found: {}", embeddingStorePath);
                return;
            }

            // Embedding Store 읽기
            logger.info("Reading embedding store from: {}", embeddingStorePath);
            String jsonContent = Files.readString(path);

            Gson gson = new Gson();
            JsonObject embeddingStore = gson.fromJson(jsonContent, JsonObject.class);
            JsonArray entries = embeddingStore.getAsJsonArray("entries");

            logger.info("Found {} segments in embedding store", entries.size());
            logger.info("-".repeat(80));

            // 각 세그먼트를 BM25에 색인
            int indexedCount = 0;
            int errorCount = 0;

            for (JsonElement entryElement : entries) {
                try {
                    JsonObject entry = entryElement.getAsJsonObject();
                    String id = entry.get("id").getAsString();

                    // TextSegment 복원
                    JsonObject embeddedObject = entry.getAsJsonObject("embedded");
                    String text = embeddedObject.get("text").getAsString();

                    // Metadata 복원
                    JsonObject metadata = embeddedObject.getAsJsonObject("metadata");
                    String regulationType = metadata != null && metadata.has("regulation_type")
                        ? metadata.get("regulation_type").getAsString()
                        : "unknown";

                    // TextSegment 생성
                    TextSegment segment = TextSegment.from(text);
                    if (metadata != null) {
                        for (Map.Entry<String, JsonElement> metaEntry : metadata.entrySet()) {
                            segment.metadata().put(
                                metaEntry.getKey(),
                                metaEntry.getValue().getAsString()
                            );
                        }
                    }

                    // BM25 인덱싱
                    bm25Service.indexSegment(segment, regulationType, id);

                    indexedCount++;
                    if (indexedCount % 10 == 0) {
                        logger.info("Indexed {} segments...", indexedCount);
                    }

                } catch (Exception e) {
                    errorCount++;
                    logger.error("Failed to index segment", e);
                }
            }

            // 커밋 (변경사항 디스크에 저장)
            logger.info("-".repeat(80));
            logger.info("Committing BM25 index...");
            bm25Service.commit();

            logger.info("=".repeat(80));
            logger.info("✓ Reindexing completed!");
            logger.info("  - Successfully indexed: {} segments", indexedCount);
            logger.info("  - Errors: {}", errorCount);
            logger.info("=".repeat(80));

        } catch (Exception e) {
            logger.error("Reindexing failed", e);
        }
    }
}
