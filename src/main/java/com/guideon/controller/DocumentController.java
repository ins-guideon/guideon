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
import com.guideon.service.RegulationSearchService;
import com.guideon.service.VectorStoreService;
import dev.langchain4j.data.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문서 업로드 및 관리 REST API Controller
 *
 * 엔드포인트:
 * - POST /api/documents/upload - PDF/DOC 문서 업로드 및 벡터 인덱싱
 * - GET /api/documents - 인덱싱된 문서 목록 조회
 * - DELETE /api/documents/{id} - 문서 삭제
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    private final TextExtractionService textExtractionService;
    private final RegulationSearchService regulationSearchService;

    public DocumentController(DocumentService documentService,
            FileStorageService fileStorageService,
            TextExtractionService textExtractionService,
            RegulationSearchService regulationSearchService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
        this.textExtractionService = textExtractionService;
        this.regulationSearchService = regulationSearchService;
    }

    /**
     * 문서 업로드 및 벡터 인덱싱
     *
     * @param file           PDF, DOC, DOCX, TXT 파일
     * @param regulationType 규정 유형
     * @return 업로드 결과
     */
    @PostMapping("/upload")
    public ApiResponse<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("regulationType") String regulationType) {

        logger.info("문서 업로드 요청: {} (type: {})", file.getOriginalFilename(), regulationType);

        try {
            // 1) temp 저장 + 텍스트 추출
            FileStorageService.TempUploadInfo info = fileStorageService.saveTemp(file, regulationType);
            String text = textExtractionService.extract(fileStorageService.getTempPath(info.getUploadId()));

            // 2) 확정과 동일한 처리: temp -> upload 이동
            FileStorageService.MoveResult move = fileStorageService.moveToUpload(info.getUploadId());

            // 3) 인덱싱(청킹/임베딩) 수행
            Document doc = Document.from(text);
            doc.metadata().put("file_name", info.getOriginalFileName());
            doc.metadata().put("regulation_type", info.getRegulationType());
            doc.metadata().put("upload_timestamp", info.getUploadTimestamp());

            regulationSearchService.indexDocument(doc, info.getRegulationType());

            // 4) 메타 저장 (내부에서 벡터 스토어까지 함께 영속화)
            DocumentMetadata metadata = documentService.addIndexedDocumentMetadata(
                    info.getOriginalFileName(),
                    info.getRegulationType(),
                    info.getFileSize(),
                    move.getFinalFileName());

            // 응답 DTO 생성
            DocumentUploadResponse response = new DocumentUploadResponse(metadata,
                    "문서가 성공적으로 업로드되고 인덱싱되었습니다.");

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
     * 텍스트 추출(프리뷰) - temp 저장 후 본문만 반환
     */
    @PostMapping("/extract-text")
    public ApiResponse<ExtractTextResponse> extractText(
            @RequestParam("file") MultipartFile file,
            @RequestParam("regulationType") String regulationType) {

        logger.info("텍스트 추출 요청: {} (type: {})", file.getOriginalFilename(), regulationType);
        try {
            // temp 저장 및 업로드 식별자 발급
            FileStorageService.TempUploadInfo info = fileStorageService.saveTemp(file, regulationType);
            // 텍스트 추출
            String text = textExtractionService.extract(fileStorageService.getTempPath(info.getUploadId()));
            return ApiResponse.success(new ExtractTextResponse(info.getUploadId(), text));
        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 텍스트 추출 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("텍스트 추출 중 오류 발생", e);
            return ApiResponse.error("텍스트 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 텍스트 확정 → 업로드 디렉토리로 이동 후 인덱싱 및 메타 저장
     */
    @PostMapping("/{uploadId}/confirm")
    public ApiResponse<DocumentUploadResponse> confirmEmbedding(
            @PathVariable String uploadId,
            @RequestBody ConfirmEmbeddingRequest request) {

        logger.info("텍스트 확정 및 인덱싱 요청: uploadId={}", uploadId);
        try {
            // temp -> upload 이동
            FileStorageService.MoveResult move = fileStorageService.moveToUpload(uploadId);
            FileStorageService.TempUploadInfo info = move.getInfo();

            // 확정 텍스트 기반 인덱싱
            Document doc = Document.from(request.getText());
            doc.metadata().put("file_name", info.getOriginalFileName());
            doc.metadata().put("regulation_type", info.getRegulationType());
            doc.metadata().put("upload_timestamp", info.getUploadTimestamp());

            regulationSearchService.indexDocument(doc, info.getRegulationType());

            // 메타 저장 (내부에서 벡터 스토어까지 함께 영속화)
            DocumentMetadata metadata = documentService.addIndexedDocumentMetadata(
                    info.getOriginalFileName(),
                    info.getRegulationType(),
                    info.getFileSize(),
                    move.getFinalFileName());

            DocumentUploadResponse response = new DocumentUploadResponse(metadata,
                    "문서가 성공적으로 확정되고 인덱싱되었습니다.");

            logger.info("확정 및 인덱싱 완료: {} (ID: {})", info.getOriginalFileName(), metadata.getId());
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            logger.warn("잘못된 확정 요청: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("확정/인덱싱 중 오류 발생", e);
            return ApiResponse.error("확정/인덱싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 인덱싱된 문서 목록 조회
     *
     * @return 문서 목록
     */
    @GetMapping
    public ApiResponse<DocumentListResponse> getDocuments() {
        logger.info("문서 목록 조회 요청");

        try {
            List<DocumentMetadata> documents = documentService.getIndexedDocuments();

            // DTO로 변환
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

    /**
     * 문서 삭제
     *
     * @param id 문서 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable String id) {
        logger.info("문서 삭제 요청: {}", id);

        try {
            String fileName = documentService.deleteDocument(id);
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
