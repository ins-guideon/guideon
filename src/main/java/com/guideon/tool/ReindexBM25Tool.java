package com.guideon.tool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.guideon.service.BM25SearchService;
import com.guideon.config.ConfigLoader;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * BM25 인덱스 재구축 도구
 * Embedding Store의 모든 세그먼트를 BM25 인덱스에 재색인
 */
public class ReindexBM25Tool {
    private static final Logger logger = LoggerFactory.getLogger(ReindexBM25Tool.class);

    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("BM25 Index Rebuild Tool - Phase 4.1");
        logger.info("=".repeat(80));

        try {
            // ConfigLoader 초기화
            ConfigLoader config = new ConfigLoader();

            // BM25SearchService 초기화
            BM25SearchService bm25Service = new BM25SearchService(config);
            logger.info("✓ BM25SearchService initialized");

            // Embedding Store 경로
            String embeddingStorePath = System.getProperty("user.home") + "/guideon/data/embedding_store.json";
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

            // 종료
            bm25Service.close();

        } catch (Exception e) {
            logger.error("Reindexing failed", e);
            System.exit(1);
        }
    }
}
