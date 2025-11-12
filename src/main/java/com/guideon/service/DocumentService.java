package com.guideon.service;

import com.guideon.model.DocumentMetadata;
import com.guideon.model.UserAccount;
import com.guideon.repository.DocumentRepository;
import com.guideon.repository.UserAccountRepository;
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

    public DocumentService(RegulationSearchService regulationSearchService,
            VectorStoreService vectorStoreService,
            DocumentRepository documentRepository,
            UserAccountRepository userAccountRepository) {
        this.regulationSearchService = regulationSearchService;
        this.vectorStoreService = vectorStoreService;
        this.documentRepository = documentRepository;
        this.userAccountRepository = userAccountRepository;
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

        dev.langchain4j.data.document.Document doc = dev.langchain4j.data.document.Document.from(content);
        doc.metadata().put("file_name", entity.getFileName());
        doc.metadata().put("regulation_type", entity.getRegulationType());
        doc.metadata().put("document_id", entity.getId());
        regulationSearchService.indexDocument(doc, entity.getRegulationType());
        vectorStoreService.saveEmbeddingStore(regulationSearchService.getEmbeddingStore());

        logger.info("Document saved and indexed: id={}, file={}", entity.getId(), entity.getFileName());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<DocumentMetadata> listIndexed() {
        return documentRepository.findAllByOrderByUploadTimeDesc();
    }

    @Transactional
    public String deleteById(String documentId) {
        String id = documentId;
        DocumentMetadata doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));
        String storageFileName = doc.getStorageFileName();
        documentRepository.delete(doc);
        return storageFileName;
    }
}
