package com.guideon.controller;

import com.guideon.dto.AnalysisResponse;
import com.guideon.dto.ApiResponse;
import com.guideon.dto.QuestionAnswerDTO;
import com.guideon.dto.QuestionRequest;
import com.guideon.dto.SearchResponse;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationSearchResult;
import com.guideon.service.QAService;
import com.guideon.service.QueryAnalysisService;
import com.guideon.service.RegulationSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Q&A REST API Controller
 *
 * 엔드포인트:
 * - POST /api/qa/ask - 질문하기 (프론트엔드용)
 * - POST /api/qa/analyze - 질문 분석
 * - POST /api/qa/search - 규정 검색 (분석 + 검색 통합)
 */
@RestController
@RequestMapping("/api/qa")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Q&A API", description = "규정 질문 및 검색 API")
public class QAController {

    private static final Logger logger = LoggerFactory.getLogger(QAController.class);

    private final QueryAnalysisService queryAnalysisService;
    private final RegulationSearchService regulationSearchService;
    private final QAService qaService;

    public QAController(QueryAnalysisService queryAnalysisService,
                       RegulationSearchService regulationSearchService,
                       QAService qaService) {
        this.queryAnalysisService = queryAnalysisService;
        this.regulationSearchService = regulationSearchService;
        this.qaService = qaService;
    }

    /**
     * 질문하기 API (프론트엔드용 통합 엔드포인트)
     *
     * @param request 사용자 질문
     * @return 질문 분석 + 답변 + 참조 규정
     */
    @Operation(summary = "질문하기", description = "규정에 대한 질문을 입력하면 AI가 답변과 함께 관련 규정을 제공합니다.")
    @PostMapping("/ask")
    public ApiResponse<QuestionAnswerDTO> askQuestion(@Valid @RequestBody QuestionRequest request) {
        logger.info("질문 요청: {}", request.getQuestion());

        try {
            QuestionAnswerDTO answer = qaService.askQuestion(request.getQuestion());
            logger.info("질문 처리 완료");
            return ApiResponse.success(answer);
        } catch (Exception e) {
            logger.error("질문 처리 중 오류 발생", e);
            return ApiResponse.error("질문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 질문 분석 API
     *
     * @param request 사용자 질문
     * @return 분석 결과 (키워드, 규정 유형, 의도, 검색 쿼리)
     */
    @Operation(summary = "질문 분석", description = "사용자 질문을 분석하여 키워드, 규정 유형, 의도 등을 추출합니다.")
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeQuestion(@Valid @RequestBody QuestionRequest request) {
        try {
            logger.info("질문 분석 요청: {}", request.getQuestion());

            QueryAnalysisResult analysis = queryAnalysisService.analyzeQuery(request.getQuestion());

            logger.info("분석 완료 - 키워드: {}, 규정 유형: {}",
                       analysis.getKeywords(), analysis.getRegulationTypes());

            return ResponseEntity.ok(AnalysisResponse.success(analysis));

        } catch (Exception e) {
            logger.error("질문 분석 실패", e);
            return ResponseEntity.internalServerError()
                    .body(AnalysisResponse.error("질문 분석 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 규정 검색 API (질문 분석 + RAG 검색 통합)
     *
     * @param request 사용자 질문
     * @return 검색 결과 (답변, 근거 조항, 신뢰도)
     */
    @Operation(summary = "규정 검색", description = "질문을 분석하고 RAG 검색을 통해 관련 규정을 검색합니다.")
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> searchRegulation(@Valid @RequestBody QuestionRequest request) {
        try {
            logger.info("규정 검색 요청: {}", request.getQuestion());

            // 1. 질문 분석
            QueryAnalysisResult analysis = queryAnalysisService.analyzeQuery(request.getQuestion());
            logger.info("질문 분석 완료 - 키워드: {}", analysis.getKeywords());

            // 2. RAG 검색 및 답변 생성
            RegulationSearchResult result = regulationSearchService.search(analysis);
            logger.info("검색 완료 - 신뢰도: {}, 참조 문서 수: {}",
                       result.getConfidenceScore(), result.getReferences().size());

            return ResponseEntity.ok(SearchResponse.success(result));

        } catch (Exception e) {
            logger.error("규정 검색 실패", e);
            return ResponseEntity.internalServerError()
                    .body(SearchResponse.error("검색 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
