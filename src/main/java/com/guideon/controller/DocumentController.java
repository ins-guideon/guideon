package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.DocumentListResponse;
import com.guideon.dto.DocumentUploadResponse;
import com.guideon.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 업로드 및 관리 REST API Controller
 *
 * 엔드포인트:
 * - POST /api/documents/upload - PDF/DOC 문서 업로드 및 벡터 인덱싱
 * - GET /api/documents - 인덱싱된 문서 목록 조회 (벡터 인덱싱용)
 * - DELETE /api/documents/{id} - 문서 삭제
 * 
 * 주의: 문서 조회 관련 API는 DocumentViewController를 사용하세요.
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "문서 관리 API", description = "문서 업로드, 조회, 삭제 API")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 문서 업로드 및 벡터 인덱싱
     *
     * @param file PDF, DOC, DOCX, TXT 파일
     * @param regulationType 규정 유형
     * @param authentication 인증 정보
     * @return 업로드 결과
     */
    @Operation(summary = "문서 업로드", description = "PDF, DOC, DOCX, TXT 파일을 업로드하고 벡터 인덱싱을 수행합니다.")
    @PostMapping("/upload")
    public ApiResponse<DocumentUploadResponse> uploadDocument(
            @Parameter(description = "업로드할 문서 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "규정 유형 (예: 이사회규정, 접대비사용규정 등)", required = true)
            @RequestParam("regulationType") String regulationType,
            Authentication authentication) {

        logger.info("문서 업로드 요청: {} (type: {})", file.getOriginalFilename(), regulationType);

        try {
            String username = authentication.getName();
            // 문서 업로드 및 인덱싱
            DocumentService.DocumentMetadata metadata = documentService.uploadAndIndexDocument(file, regulationType, username);

            // 응답 DTO 생성
            DocumentUploadResponse response = new DocumentUploadResponse(
                metadata.getId(),
                metadata.getFileName(),
                metadata.getRegulationType(),
                metadata.getFileSize(),
                metadata.getUploadTimestamp(),
                metadata.getStatus(),
                "문서가 성공적으로 업로드되고 인덱싱되었습니다."
            );

            logger.info("문서 업로드 완료: {} (ID: {})", file.getOriginalFilename(), metadata.getId());
            return ApiResponse.success(response);

        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 문서 업로드 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());

        } catch (Exception e) {
            logger.error("문서 업로드 중 오류 발생", e);
            return ApiResponse.error("문서 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 인덱싱된 문서 목록 조회 (벡터 인덱싱용)
     *
     * @return 문서 목록
     */
    @Operation(summary = "문서 목록 조회", description = "인덱싱된 모든 문서 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<DocumentListResponse> getDocuments() {
        logger.info("문서 목록 조회 요청");

        try {
            List<DocumentService.DocumentMetadata> documents = documentService.getIndexedDocuments();

            // DTO로 변환
            List<DocumentListResponse.DocumentInfo> documentInfos = documents.stream()
                .map(doc -> new DocumentListResponse.DocumentInfo(
                    doc.getId(),
                    doc.getFileName(),
                    doc.getRegulationType(),
                    doc.getFileSize(),
                    doc.getUploadTimestamp(),
                    doc.getStatus()
                ))
                .collect(Collectors.toList());

            DocumentListResponse response = new DocumentListResponse(documentInfos, documentInfos.size());

            logger.info("문서 목록 조회 완료: {} 건", documentInfos.size());
            return ApiResponse.success(response);

        } catch (Exception e) {
            logger.error("문서 목록 조회 중 오류 발생", e);
            return ApiResponse.error("문서 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    /**
     * 문서 삭제
     *
     * @param id 문서 ID
     * @return 삭제 결과
     */
    @Operation(summary = "문서 삭제", description = "지정된 ID의 문서를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(
            @Parameter(description = "삭제할 문서 ID", required = true)
            @PathVariable String id) {
        logger.info("문서 삭제 요청: {}", id);

        try {
            documentService.deleteDocument(id);

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

    /**
     * 헬스 체크
     *
     * @return 서비스 상태
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document service is running");
    }
}
