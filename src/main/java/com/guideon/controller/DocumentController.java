package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.DocumentListResponse;
import com.guideon.dto.DocumentUploadResponse;
import com.guideon.dto.ExtractTextResponse;
import com.guideon.dto.ConfirmEmbeddingRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
            @Parameter(description = "업로드할 문서 파일", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "규정 유형 (예: 이사회규정, 접대비사용규정 등)", required = true) @RequestParam("regulationType") String regulationType,
            Authentication authentication) {

        logger.info("문서 업로드 요청: {} (type: {})", file.getOriginalFilename(), regulationType);

        try {
            // 업로드 디렉토리에 바로 저장 → 텍스트 추출
            FileStorageService.UploadResult up = fileStorageService.saveToUpload(file, regulationType);
            String content = textExtractionService.extract(up.getPath());

            // DB 저장 및 인덱싱
            com.guideon.model.DocumentMetadata saved = documentService.saveAndIndex(
                    up.getId(),
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
            FileStorageService.TempUploadInfo info = fileStorageService.saveTemp(file, regulationType);
            String text = textExtractionService.extract(fileStorageService.getTempPath(info.getId()));
            return ApiResponse.success(new ExtractTextResponse(info.getId(), text));
        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 텍스트 추출 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("텍스트 추출 중 오류 발생", e);
            return ApiResponse.error("텍스트 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/{uploadId}/confirm")
    public ApiResponse<DocumentUploadResponse> confirmEmbedding(
            @PathVariable String uploadId,
            @RequestBody ConfirmEmbeddingRequest request,
            Authentication authentication) {

        logger.info("텍스트 확정 및 인덱싱 요청: uploadId={}", uploadId);
        try {
            FileStorageService.MoveResult move = fileStorageService.moveToUpload(uploadId);
            FileStorageService.TempUploadInfo info = move.getInfo();
            DocumentMetadata saved = documentService.saveAndIndex(
                    info.getId(),
                    request.getText(),
                    info.getRegulationType(),
                    info.getOriginalFileName(),
                    move.getFinalFileName(),
                    info.getFileSize(),
                    authentication);
            DocumentUploadResponse response = new DocumentUploadResponse(saved,
                    "문서가 성공적으로 확정되고 인덱싱되었습니다.");
            logger.info("확정 및 인덱싱 완료: {} (ID: {})", saved.getFileName(), saved.getId());
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 확정 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("확정/인덱싱 중 오류 발생", e);
            return ApiResponse.error("확정/인덱싱 중 오류가 발생했습니다: " + e.getMessage());
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

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document service is running");
    }
}
