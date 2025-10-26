package com.guideon.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private final DocumentParser pdfParser;
    private final DocumentParser docParser;

    // 업로드된 파일 저장 경로
    private final String uploadDir;

    // 인덱싱된 문서 정보 저장 (실제로는 DB에 저장해야 함)
    private final List<DocumentMetadata> indexedDocuments;

    public DocumentService(RegulationSearchService regulationSearchService,
                          VectorStoreService vectorStoreService) {
        this.regulationSearchService = regulationSearchService;
        this.vectorStoreService = vectorStoreService;
        this.pdfParser = new ApachePdfBoxDocumentParser();
        this.docParser = new ApachePoiDocumentParser();
        this.uploadDir = System.getProperty("user.home") + "/guideon/uploads";
        this.indexedDocuments = new ArrayList<>();

        // 업로드 디렉토리 생성
        try {
            Files.createDirectories(Paths.get(uploadDir));
            logger.info("Upload directory initialized: {}", uploadDir);
        } catch (Exception e) {
            logger.error("Failed to create upload directory", e);
        }

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
                regulationSearchService.getEmbeddingStore()
            );

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
     * 문서 업로드 및 인덱싱
     */
    public DocumentMetadata uploadAndIndexDocument(MultipartFile file, String regulationType) {
        logger.info("Processing document upload: {} (type: {})", file.getOriginalFilename(), regulationType);

        try {
            // 1. 파일 검증
            validateFile(file);

            // 2. 파일 저장
            String savedFileName = saveFile(file);
            Path savedFilePath = Paths.get(uploadDir, savedFileName);

            // 3. 문서 파싱
            Document document = parseDocument(file, savedFilePath);

            // 4. 메타데이터 추가
            document.metadata().put("file_name", file.getOriginalFilename());
            document.metadata().put("regulation_type", regulationType);
            document.metadata().put("upload_timestamp", System.currentTimeMillis());

            // 5. 벡터 인덱싱
            regulationSearchService.indexDocument(document, regulationType);

            // 6. 메타데이터 저장
            DocumentMetadata metadata = new DocumentMetadata(
                UUID.randomUUID().toString(),
                file.getOriginalFilename(),
                regulationType,
                file.getSize(),
                savedFileName,
                System.currentTimeMillis(),
                "indexed"
            );

            indexedDocuments.add(metadata);

            // 벡터 데이터 및 메타데이터 저장
            saveToFileSystem();

            logger.info("Document indexed successfully: {}", file.getOriginalFilename());
            return metadata;

        } catch (Exception e) {
            logger.error("Failed to process document", e);
            throw new RuntimeException("문서 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }

        // 지원하는 파일 형식 확인
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isSupportedFileType(extension)) {
            throw new IllegalArgumentException(
                "지원하지 않는 파일 형식입니다. (지원 형식: PDF, DOC, DOCX, TXT)"
            );
        }

        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    /**
     * 파일 저장
     */
    private String saveFile(MultipartFile file) throws Exception {
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String savedFileName = UUID.randomUUID().toString() + "." + extension;

        Path targetPath = Paths.get(uploadDir, savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        logger.debug("File saved: {}", savedFileName);
        return savedFileName;
    }

    /**
     * 문서 파싱
     */
    private Document parseDocument(MultipartFile file, Path filePath) throws Exception {
        String fileName = file.getOriginalFilename();
        String extension = getFileExtension(fileName).toLowerCase();

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            Document document;

            switch (extension) {
                case "pdf":
                    document = pdfParser.parse(inputStream);
                    break;

                case "doc":
                case "docx":
                    document = docParser.parse(inputStream);
                    break;

                case "txt":
                    // 텍스트 파일은 직접 읽기
                    String content = new String(Files.readAllBytes(filePath));
                    document = Document.from(content);
                    break;

                default:
                    throw new IllegalArgumentException("지원하지 않는 파일 형식: " + extension);
            }

            logger.debug("Document parsed successfully: {} ({} characters)",
                fileName, document.text().length());

            return document;
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 지원하는 파일 타입 확인
     */
    private boolean isSupportedFileType(String extension) {
        return extension.equals("pdf") ||
               extension.equals("doc") ||
               extension.equals("docx") ||
               extension.equals("txt");
    }

    /**
     * 인덱싱된 문서 목록 조회
     */
    public List<DocumentMetadata> getIndexedDocuments() {
        return new ArrayList<>(indexedDocuments);
    }

    /**
     * 문서 삭제
     */
    public void deleteDocument(String documentId) {
        logger.info("Deleting document: {}", documentId);

        DocumentMetadata metadata = indexedDocuments.stream()
            .filter(doc -> doc.getId().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + documentId));

        try {
            // 파일 삭제
            Path filePath = Paths.get(uploadDir, metadata.getSavedFileName());
            Files.deleteIfExists(filePath);

            // 메타데이터 삭제
            indexedDocuments.remove(metadata);

            // 변경사항 저장
            saveToFileSystem();

            logger.info("Document deleted successfully: {}", documentId);

        } catch (Exception e) {
            logger.error("Failed to delete document", e);
            throw new RuntimeException("문서 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 문서 메타데이터 내부 클래스
     */
    public static class DocumentMetadata {
        private final String id;
        private final String fileName;
        private final String regulationType;
        private final long fileSize;
        private final String savedFileName;
        private final long uploadTimestamp;
        private final String status;

        public DocumentMetadata(String id, String fileName, String regulationType,
                              long fileSize, String savedFileName, long uploadTimestamp, String status) {
            this.id = id;
            this.fileName = fileName;
            this.regulationType = regulationType;
            this.fileSize = fileSize;
            this.savedFileName = savedFileName;
            this.uploadTimestamp = uploadTimestamp;
            this.status = status;
        }

        public String getId() { return id; }
        public String getFileName() { return fileName; }
        public String getRegulationType() { return regulationType; }
        public long getFileSize() { return fileSize; }
        public String getSavedFileName() { return savedFileName; }
        public long getUploadTimestamp() { return uploadTimestamp; }
        public String getStatus() { return status; }
    }
}
