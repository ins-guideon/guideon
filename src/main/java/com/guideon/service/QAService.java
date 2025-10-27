package com.guideon.service;

import com.guideon.dto.QuestionAnalysisDTO;
import com.guideon.dto.QuestionAnswerDTO;
import com.guideon.dto.QuestionReferenceDTO;
import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationReference;
import com.guideon.model.RegulationSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * QA 서비스 - 질문 분석 및 답변 생성을 통합 처리
 */
@Service
public class QAService {
    private static final Logger logger = LoggerFactory.getLogger(QAService.class);

    private final QueryAnalysisService queryAnalysisService;
    private final RegulationSearchService regulationSearchService;

    public QAService(QueryAnalysisService queryAnalysisService,
                     RegulationSearchService regulationSearchService) {
        this.queryAnalysisService = queryAnalysisService;
        this.regulationSearchService = regulationSearchService;
    }

    /**
     * 사용자 질문에 대한 답변 생성
     */
    public QuestionAnswerDTO askQuestion(String question) {
        logger.info("Processing question: {}", question);

        try {
            // 1. 질문 분석
            QueryAnalysisResult analysisResult = queryAnalysisService.analyzeQuery(question);
            logger.debug("Question analysis completed: {}", analysisResult);

            // 2. 규정 검색 및 답변 생성
            RegulationSearchResult searchResult = regulationSearchService.search(analysisResult);
            logger.debug("Search completed with confidence: {}", searchResult.getConfidenceScore());

            // 3. DTO로 변환
            QuestionAnalysisDTO analysisDTO = convertToAnalysisDTO(analysisResult);
            List<QuestionReferenceDTO> referenceDTOs = convertToReferenceDTOs(searchResult.getReferences());

            QuestionAnswerDTO answerDTO = new QuestionAnswerDTO(
                searchResult.getAnswer(),
                analysisDTO,
                referenceDTOs,
                searchResult.getConfidenceScore()
            );

            logger.info("Question processed successfully");
            return answerDTO;

        } catch (Exception e) {
            logger.error("Error processing question", e);
            throw new RuntimeException("질문 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * QueryAnalysisResult를 QuestionAnalysisDTO로 변환
     */
    private QuestionAnalysisDTO convertToAnalysisDTO(QueryAnalysisResult analysisResult) {
        return new QuestionAnalysisDTO(
            analysisResult.getKeywords(),
            analysisResult.getRegulationTypes(),
            analysisResult.getIntent()
        );
    }

    /**
     * RegulationReference 리스트를 QuestionReferenceDTO 리스트로 변환
     */
    private List<QuestionReferenceDTO> convertToReferenceDTOs(List<RegulationReference> references) {
        return references.stream()
            .map(ref -> new QuestionReferenceDTO(
                ref.getDocumentName(),
                ref.getContent(),
                ref.getRelevanceScore()
            ))
            .collect(Collectors.toList());
    }
}
