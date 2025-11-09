package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.DocumentDetailResponse;
import com.guideon.dto.DocumentListResponse;
import com.guideon.model.Document;
import com.guideon.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 조회 REST API Controller
 *
 * 엔드포인트:
 * - GET /api/documents/view - 문서 목록 조회 (DB에서 조회)
 * - GET /api/documents/view/{id} - 문서 상세 조회
 */
@RestController
@RequestMapping("/api/documents/view")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "문서 조회 API", description = "DB에서 문서 목록 및 상세 정보를 조회하는 API")
public class DocumentViewController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentViewController.class);

    private final DocumentService documentService;

    public DocumentViewController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * DB에서 문서 목록 조회 (문서 조회 페이지용)
     *
     * @return 문서 목록
     */
    @Operation(summary = "문서 목록 조회", description = "DB에 저장된 모든 문서 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<DocumentListResponse> getDocumentsForView() {
        logger.info("문서 조회 목록 요청");

        try {
            List<Document> documents = documentService.getAllDocuments();

            // DTO로 변환
            List<DocumentListResponse.DocumentInfo> documentInfos = documents.stream()
                .map(doc -> new DocumentListResponse.DocumentInfo(
                    String.valueOf(doc.getId()),
                    doc.getFileName(),
                    doc.getRegulationType(),
                    doc.getFileSize(),
                    doc.getUploadTime().toEpochMilli(),
                    doc.getStatus()
                ))
                .collect(Collectors.toList());

            DocumentListResponse response = new DocumentListResponse(documentInfos, documentInfos.size());

            logger.info("문서 조회 목록 완료: {} 건", documentInfos.size());
            return ApiResponse.success(response);

        } catch (Exception e) {
            logger.error("문서 조회 목록 중 오류 발생", e);
            return ApiResponse.error("문서 조회 목록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 문서 상세 조회
     *
     * @param id 문서 ID
     * @return 문서 상세 정보
     */
    @Operation(summary = "문서 상세 조회", description = "지정된 ID의 문서 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<DocumentDetailResponse> getDocumentDetail(
            @Parameter(description = "조회할 문서 ID", required = true)
            @PathVariable Long id) {
        logger.info("문서 상세 조회 요청: {}", id);

        try {
            Document document = documentService.getDocumentById(id);

            DocumentDetailResponse response = new DocumentDetailResponse(
                document.getId(),
                document.getFileName(),
                document.getRegulationType(),
                document.getUploadTime().toEpochMilli(),
                document.getContent(),
                document.getUploader().getName(),
                document.getFileSize(),
                document.getStatus()
            );

            logger.info("문서 상세 조회 완료: {}", id);
            return ApiResponse.success(response);

        } catch (IllegalArgumentException e) {
            logger.warn("존재하지 않는 문서 조회 요청: {}", id);
            return ApiResponse.error(e.getMessage());

        } catch (Exception e) {
            logger.error("문서 상세 조회 중 오류 발생", e);
            return ApiResponse.error("문서 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

