package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.DocumentListResponse;
import com.guideon.dto.DocumentUploadResponse;
import com.guideon.dto.ExtractTextResponse;
import com.guideon.model.DocumentMetadata;
import com.guideon.service.DocumentService;
import com.guideon.service.FileStorageService;
import com.guideon.service.TextExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "문서 관리 API", description = "문서 업로드, 조회, 삭제 API")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final TextExtractionService textExtractionService;

    public DocumentController(DocumentService documentService,
            FileStorageService fileStorageService,
            TextExtractionService textExtractionService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.textExtractionService = textExtractionService;
    }

    @Operation(summary = "문서 업로드", description = "PDF, DOC, DOCX, TXT 파일을 업로드하고 벡터 인덱싱을 수행합니다.")
    @PostMapping("/upload")
    public ApiResponse<DocumentUploadResponse> uploadDocument(
            @Parameter(required = true) @RequestParam("file") MultipartFile file,
            @Parameter(required = true) @RequestParam("regulationType") String regulationType,
            @Parameter(required = true) @RequestParam(value = "content", required = false) String content,
            Authentication authentication) {

        logger.info("문서 업로드 요청: {} (type: {}, hasContent: {})", file.getOriginalFilename(), regulationType,
                content != null && !content.isEmpty());

        try {
            // content가 null이거나 공백이면 예외 발생
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("문서 내용(content)이 필수입니다.");
            }

            String documentId = UUID.randomUUID().toString();

            // 업로드 디렉토리에 파일 저장
            FileStorageService.UploadResult up = fileStorageService.saveToUpload(file, regulationType, documentId);

            // DB 저장 및 인덱싱
            DocumentMetadata saved = documentService.saveAndIndex(
                    documentId,
                    content,
                    regulationType,
                    up.getOriginalFileName(),
                    up.getSavedFileName(),
                    up.getFileSize(),
                    authentication);
            DocumentUploadResponse response = new DocumentUploadResponse(saved,
                    "문서가 성공적으로 업로드되고 인덱싱되었습니다.");
            logger.info("문서 업로드 완료: {} (ID: {})", file.getOriginalFilename(), saved.getId());
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 문서 업로드 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("문서 업로드 중 오류 발생", e);
            return ApiResponse.error("문서 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/extract-text")
    public ApiResponse<ExtractTextResponse> extractText(
            @RequestParam("file") MultipartFile file,
            @RequestParam("regulationType") String regulationType) {

        logger.info("텍스트 추출 요청: {} (type: {})", file.getOriginalFilename(), regulationType);
        try {
            // 파일 유효성 검증
            fileStorageService.validateFile(file);

            // 텍스트 추출 (MultipartFile 직접 사용)
            String text = textExtractionService.extract(file);

            return ApiResponse.success(new ExtractTextResponse(text));
        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 텍스트 추출 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("텍스트 추출 중 오류 발생", e);
            return ApiResponse.error("텍스트 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "문서 목록 조회", description = "인덱싱된 모든 문서 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<DocumentListResponse> getDocuments() {
        logger.info("문서 목록 조회 요청");

        try {
            List<DocumentMetadata> documents = documentService.listIndexed();
            List<DocumentListResponse.DocumentInfo> documentInfos = documents.stream()
                    .map(DocumentListResponse.DocumentInfo::new)
                    .collect(Collectors.toList());

            DocumentListResponse response = new DocumentListResponse(documentInfos, documentInfos.size());

            logger.info("문서 목록 조회 완료: {} 건", documentInfos.size());
            return ApiResponse.success(response);

        } catch (Exception e) {
            logger.error("문서 목록 조회 중 오류 발생", e);
            return ApiResponse.error("문서 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "문서 삭제", description = "지정된 ID의 문서를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(
            @Parameter(description = "삭제할 문서 ID", required = true) @PathVariable String id) {
        logger.info("문서 삭제 요청: {}", id);

        try {
            String fileName = documentService.deleteById(id);
            fileStorageService.deleteFromUpload(fileName);

            logger.info("문서 삭제 완료: {}", id);
            return ApiResponse.success(null, "문서가 성공적으로 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            logger.warn("존재하지 않는 문서 삭제 요청: {}", id);
            return ApiResponse.error(e.getMessage());

        } catch (Exception e) {
            logger.error("문서 삭제 중 오류 발생", e);
            return ApiResponse.error("문서 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "문서 업데이트", description = "기존 문서를 삭제하고 새 파일로 업데이트합니다. 파일이 없으면 기존 파일을 유지합니다.")
    @PostMapping("/{documentId}/update")
    public ApiResponse<DocumentUploadResponse> updateDocument(
            @Parameter(required = true) @PathVariable String documentId,
            @Parameter(required = false) @RequestParam(value = "file", required = false) MultipartFile file,
            @Parameter(required = true) @RequestParam("text") String text,
            @Parameter(required = true) @RequestParam("regulationType") String regulationType,
            Authentication authentication) {

        logger.info("문서 업데이트 요청: {} (type: {}, hasFile: {})", documentId, regulationType,
                file != null && !file.isEmpty());

        try {
            // 기존 문서 정보 조회
            DocumentMetadata metadata = documentService.findById(documentId);

            if (file != null && !file.isEmpty()) {
                // 기존 파일 삭제
                fileStorageService.deleteFromUpload(metadata.getStorageFileName());

                // 새 파일 저장
                FileStorageService.UploadResult uploadResult = fileStorageService.saveToUpload(file, regulationType,
                        documentId);
                metadata.setStorageFileName(uploadResult.getSavedFileName());
                metadata.setFileName(uploadResult.getOriginalFileName());
                metadata.setFileSize(uploadResult.getFileSize());
                logger.info("새 파일 저장 완료: {}", metadata.getStorageFileName());

            }

            // 기존 문서 삭제 (임베딩 포함)
            documentService.deleteById(documentId);

            // 문서 저장 및 인덱싱 (기존 ID 사용)
            DocumentMetadata saved = documentService.saveAndIndex(
                    documentId,
                    text,
                    regulationType,
                    metadata.getFileName(),
                    metadata.getStorageFileName(),
                    metadata.getFileSize(),
                    authentication);

            DocumentUploadResponse response = new DocumentUploadResponse(saved,
                    "문서가 성공적으로 업데이트되고 인덱싱되었습니다.");
            logger.info("문서 업데이트 완료: {} (ID: {})", saved.getFileName(), saved.getId());
            return ApiResponse.success(response);

        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 문서 업데이트 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());

        } catch (Exception e) {
            logger.error("문서 업데이트 중 오류 발생", e);
            return ApiResponse.error("문서 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document service is running");
    }
}
