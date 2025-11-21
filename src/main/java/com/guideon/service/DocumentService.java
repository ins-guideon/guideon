package com.guideon.service;

import com.guideon.model.DocumentMetadata;
import com.guideon.model.UserAccount;
import com.guideon.repository.DocumentRepository;
import com.guideon.repository.UserAccountRepository;
import dev.langchain4j.data.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final RegulationSearchService regulationSearchService;
    private final VectorStoreService vectorStoreService;
    private final DocumentRepository documentRepository;
    private final UserAccountRepository userAccountRepository;
    private final HybridSearchService hybridSearchService;

    public DocumentService(RegulationSearchService regulationSearchService,
            VectorStoreService vectorStoreService,
            DocumentRepository documentRepository,
            UserAccountRepository userAccountRepository,
            HybridSearchService hybridSearchService) {
        this.regulationSearchService = regulationSearchService;
        this.vectorStoreService = vectorStoreService;
        this.documentRepository = documentRepository;
        this.userAccountRepository = userAccountRepository;
        this.hybridSearchService = hybridSearchService;
    }

    /**
     * 컨트롤러에서 파일 저장/텍스트 추출을 마친 뒤 호출.
     * DB 저장 → 임베딩 인덱싱 → 벡터 스토어 영속화
     */
    @Transactional
    public DocumentMetadata saveAndIndex(String id,
            String content,
            String regulationType,
            String originalFileName,
            String storageFileName,
            long fileSize,
            Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        UserAccount uploader = null;
        if (username != null) {
            uploader = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        }

        DocumentMetadata entity = new DocumentMetadata(id, originalFileName, regulationType, Instant.now(), content,
                uploader, fileSize, storageFileName);
        entity.setStorageFileName(storageFileName);
        entity = documentRepository.save(entity);

        Document doc = Document.from(content);
        doc.metadata().put(DocumentMetadata.FILENAME, entity.getFileName());
        doc.metadata().put(DocumentMetadata.REGULATION_TYPE, entity.getRegulationType());
        doc.metadata().put(DocumentMetadata.DOCUMENT_ID, entity.getId());
        regulationSearchService.indexDocument(doc, entity.getRegulationType());
        vectorStoreService.saveEmbeddingStore(regulationSearchService.getEmbeddingStore());

        logger.info("Document saved and indexed: id={}, file={}", entity.getId(), entity.getFileName());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<DocumentMetadata> listIndexed() {
        return documentRepository.findAllByOrderByUploadTimeDesc();
    }

    @Transactional(readOnly = true)
    public DocumentMetadata findById(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));
    }

    @Transactional
    public String deleteById(String documentId) {
        String id = documentId;
        DocumentMetadata doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));
        String storageFileName = doc.getStorageFileName();

        // 임베딩 삭제: Vector Store에서 삭제
        try {
            int deletedEmbeddings = regulationSearchService.deleteDocumentEmbeddings(documentId);
            logger.info("Deleted {} embeddings from Vector Store for document: {}", deletedEmbeddings, documentId);

            // Vector Store 영속화
            vectorStoreService.saveEmbeddingStore(regulationSearchService.getEmbeddingStore());
        } catch (Exception e) {
            logger.error("Failed to delete embeddings from Vector Store for document: {}", documentId, e);
            // 임베딩 삭제 실패해도 문서 삭제는 계속 진행
        }

        // BM25 인덱스에서 삭제 (Hybrid Search가 활성화된 경우)
        if (hybridSearchService != null) {
            try {
                int deletedSegments = hybridSearchService.deleteDocumentSegments(documentId);
                logger.info("Deleted {} segments from BM25 index for document: {}", deletedSegments, documentId);

                // BM25 인덱스 커밋
                hybridSearchService.commit();
            } catch (Exception e) {
                logger.error("Failed to delete segments from BM25 index for document: {}", documentId, e);
                // BM25 삭제 실패해도 문서 삭제는 계속 진행
            }
        }

        // DB에서 문서 삭제
        documentRepository.delete(doc);
        logger.info("Document deleted: id={}, file={}", documentId, storageFileName);

        return storageFileName;
    }
}
