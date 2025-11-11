package com.guideon.service;

import com.guideon.model.DocumentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 문서 업로드 및 처리 서비스
 * PDF, DOC, DOCX 등의 문서를 파싱하고 벡터 데이터베이스에 인덱싱
 */
@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final RegulationSearchService regulationSearchService;
    private final VectorStoreService vectorStoreService;

    // 인덱싱된 문서 정보 저장 (실제로는 DB에 저장해야 함)
    private final List<DocumentMetadata> indexedDocuments;

    public DocumentService(RegulationSearchService regulationSearchService,
            VectorStoreService vectorStoreService) {
        this.regulationSearchService = regulationSearchService;
        this.vectorStoreService = vectorStoreService;
        this.indexedDocuments = new ArrayList<>();

        // 기존 메타데이터 로드
        loadPersistedMetadata();
    }

    /**
     * 벡터 데이터 및 메타데이터를 파일시스템에 저장
     */
    private void saveToFileSystem() {
        try {
            // 벡터 스토어 저장
            vectorStoreService.saveEmbeddingStore(
                    regulationSearchService.getEmbeddingStore());

            // 메타데이터 저장
            vectorStoreService.saveMetadata(indexedDocuments);

            logger.info("Vector data and metadata saved to filesystem");
        } catch (Exception e) {
            logger.error("Failed to save data to filesystem", e);
        }
    }

    /**
     * 저장된 메타데이터 로드
     */
    private void loadPersistedMetadata() {
        try {
            List<DocumentMetadata> persistedMetadata = vectorStoreService.loadMetadata();
            indexedDocuments.addAll(persistedMetadata);
            logger.info("Loaded {} documents from persisted metadata", persistedMetadata.size());
        } catch (Exception e) {
            logger.error("Failed to load persisted metadata", e);
        }
    }

    /**
     * 인덱싱된 문서 목록 조회
     */
    public List<DocumentMetadata> getIndexedDocuments() {
        return new ArrayList<>(indexedDocuments);
    }

    /**
     * 확인 완료된 문서에 대해 메타데이터를 생성/저장한다.
     * (임베딩 인덱싱이 이미 완료되었다고 가정)
     */
    public DocumentMetadata addIndexedDocumentMetadata(String originalFileName, String regulationType, long fileSize,
            String savedFileName) {
        DocumentMetadata metadata = new DocumentMetadata(
                UUID.randomUUID().toString(),
                originalFileName,
                regulationType,
                fileSize,
                savedFileName,
                System.currentTimeMillis(),
                "indexed");
        indexedDocuments.add(metadata);
        saveToFileSystem();
        return metadata;
    }

    /**
     * 문서 삭제
     */
    public String deleteDocument(String documentId) {
        logger.info("Deleting document: {}", documentId);

        DocumentMetadata metadata = indexedDocuments.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));

        try {
            // 메타데이터 삭제
            indexedDocuments.remove(metadata);

            // 변경사항 저장
            saveToFileSystem();

            logger.info("Document deleted successfully: {}", documentId);

            return metadata.getSavedFileName();
        } catch (Exception e) {
            logger.error("Failed to delete document", e);
            throw new RuntimeException("문서 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

}
