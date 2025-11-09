package com.guideon.controller;

import com.guideon.dto.UploadRequest;
import com.guideon.service.RegulationSearchService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 규정 문서 관리 REST API Controller
 *
 * 엔드포인트:
 * - POST /api/regulations/upload - 규정 문서 업로드 및 인덱싱
 * - GET /api/regulations/types - 지원하는 규정 유형 목록 조회
 */
@RestController
@RequestMapping("/api/regulations")
@Tag(name = "규정 관리 API", description = "규정 문서 업로드 및 규정 유형 조회 API")
public class RegulationController {

    private static final Logger logger = LoggerFactory.getLogger(RegulationController.class);

    private final RegulationSearchService regulationSearchService;

    // 지원하는 규정 유형 (CLAUDE.md 기준)
    private static final String[] REGULATION_TYPES = {
        "이사회규정", "접대비사용규정", "윤리규정", "출장여비지급규정",
        "주식매수선택권운영규정", "노사협의회규정", "취업규칙", "매출채권관리규정",
        "금융자산 운용규정", "문서관리규정", "재고관리규정", "계약검토규정",
        "사규관리규정", "임원퇴직금지급규정", "임원보수규정", "주주총회운영규정",
        "경비지급규정", "복리후생비규정", "보안관리규정", "위임전결규정",
        "우리사주운영규정", "내부정보관리규정", "회계관리규정", "특수관계자 거래규정",
        "조직 및 업무분장규정", "자금관리규정", "인장관리규정"
    };

    public RegulationController(RegulationSearchService regulationSearchService) {
        this.regulationSearchService = regulationSearchService;
    }

    /**
     * 규정 문서 업로드 및 인덱싱 API
     *
     * @param request 파일 경로 및 규정 유형
     * @return 업로드 결과
     */
    @Operation(summary = "규정 문서 업로드", description = "파일 시스템 경로를 통해 규정 문서를 업로드하고 인덱싱합니다.")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadRegulation(@Valid @RequestBody UploadRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("규정 문서 업로드 요청 - 파일: {}, 유형: {}",
                       request.getFilePath(), request.getRegulationType());

            // 파일 로드
            Path filePath = Paths.get(request.getFilePath());
            Document document = FileSystemDocumentLoader.loadDocument(filePath);

            // 문서 인덱싱
            regulationSearchService.indexDocument(document, request.getRegulationType());

            logger.info("규정 문서 인덱싱 완료: {}", request.getRegulationType());

            response.put("success", true);
            response.put("message", "규정 문서가 성공적으로 업로드되었습니다");
            response.put("regulationType", request.getRegulationType());
            response.put("filePath", request.getFilePath());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("규정 문서 업로드 실패", e);

            response.put("success", false);
            response.put("message", "업로드 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 지원하는 규정 유형 목록 조회 API
     *
     * @return 27개 규정 유형 목록
     */
    @Operation(summary = "규정 유형 목록 조회", description = "지원하는 모든 규정 유형 목록을 조회합니다.")
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getRegulationTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("types", REGULATION_TYPES);
        response.put("count", REGULATION_TYPES.length);

        return ResponseEntity.ok(response);
    }
}
