package com.guideon.util;

import com.guideon.model.QueryAnalysisResult;
import com.guideon.model.RegulationArticle;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 향상된 컨텍스트 빌더
 * 검색 결과를 구조화된 형태로 변환하여 LLM이 더 잘 이해할 수 있도록 함
 */
public class EnhancedContextBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedContextBuilder.class);

    /**
     * 구조화된 컨텍스트 생성
     *
     * @param segments 검색된 세그먼트 목록
     * @param analysis 질문 분석 결과 (선택사항)
     * @return 구조화된 컨텍스트 문자열
     */
    public static String buildStructuredContext(
            List<EmbeddingMatch<TextSegment>> segments,
            QueryAnalysisResult analysis) {

        logger.info("========================================");
        logger.info("Building STRUCTURED CONTEXT (Phase 3)");
        logger.info("========================================");

        if (segments == null || segments.isEmpty()) {
            logger.warn("No segments provided for context building");
            return "";
        }

        logger.info("Input: {} search result segments", segments.size());

        StringBuilder context = new StringBuilder();

        // 헤더 추가
        context.append("=== 검색된 규정 내용 ===\n\n");

        int totalArticlesExtracted = 0;
        int segmentsWithArticles = 0;

        for (int i = 0; i < segments.size(); i++) {
            EmbeddingMatch<TextSegment> match = segments.get(i);
            TextSegment segment = match.embedded();
            double score = match.score();

            // 메타데이터 추출
            String regulationType = segment.metadata().getString("regulation_type");
            if (regulationType == null || regulationType.isEmpty()) {
                regulationType = "알 수 없음";
            }

            // 조항 정보 추출
            String text = segment.text();
            List<RegulationArticle> articles = RegulationArticleExtractor.extractArticles(text, regulationType);
            String firstArticle = RegulationArticleExtractor.extractFirstArticleNumber(text);

            // 로그: 각 세그먼트 처리 정보
            logger.info("  [Segment {}] Score: {:.3f}, Source: {}, Article: {}, Text length: {} chars",
                    i + 1,
                    score,
                    regulationType,
                    firstArticle != null ? firstArticle : "없음",
                    text.length());

            if (!articles.isEmpty()) {
                segmentsWithArticles++;
                totalArticlesExtracted += articles.size();
                logger.info("    → Extracted {} article(s): {}",
                        articles.size(),
                        articles.stream()
                                .map(RegulationArticle::getArticleNumber)
                                .collect(Collectors.joining(", ")));
            }

            // 구조화된 정보 추가
            context.append(String.format("[검색 결과 %d]", i + 1));

            // 관련도 점수 추가 (높을수록 중요)
            context.append(String.format(" (관련도: %.2f", score));

            // 출처 정보
            context.append(String.format(", 출처: %s", regulationType));

            // 조항 번호가 있으면 추가
            if (firstArticle != null) {
                context.append(String.format(", %s", firstArticle));
            }

            context.append(")\n");

            // 조항이 명확히 구조화되어 있는 경우
            if (!articles.isEmpty()) {
                for (RegulationArticle article : articles) {
                    context.append(article.toFormattedString()).append("\n");
                }
            } else {
                // 조항 구조가 없는 경우 원문 그대로
                context.append(text).append("\n");
            }

            context.append("\n");

            // 구분선 (마지막이 아닌 경우)
            if (i < segments.size() - 1) {
                context.append("---\n\n");
            }
        }

        String result = context.toString();

        // 최종 요약 로그
        logger.info("----------------------------------------");
        logger.info("STRUCTURED CONTEXT SUMMARY:");
        logger.info("  Total segments: {}", segments.size());
        logger.info("  Segments with articles: {}", segmentsWithArticles);
        logger.info("  Total articles extracted: {}", totalArticlesExtracted);
        logger.info("  Final context length: {} chars", result.length());
        logger.info("  Metadata included: ✓ Scores, ✓ Sources, ✓ Article numbers");
        logger.info("========================================");

        return result;
    }

    /**
     * 간단한 컨텍스트 생성 (기존 방식)
     *
     * @param segments 검색된 세그먼트 목록
     * @return 간단한 컨텍스트 문자열
     */
    public static String buildSimpleContext(List<EmbeddingMatch<TextSegment>> segments) {
        if (segments == null || segments.isEmpty()) {
            return "";
        }

        return segments.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 요약된 컨텍스트 생성 (길이 제한)
     *
     * @param segments 검색된 세그먼트 목록
     * @param maxLength 최대 길이 (문자 수)
     * @return 요약된 컨텍스트
     */
    public static String buildSummaryContext(
            List<EmbeddingMatch<TextSegment>> segments,
            int maxLength) {

        if (segments == null || segments.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        int currentLength = 0;

        for (int i = 0; i < segments.size(); i++) {
            EmbeddingMatch<TextSegment> match = segments.get(i);
            String text = match.embedded().text();
            double score = match.score();

            // 메타데이터
            String regulationType = match.embedded().metadata().getString("regulation_type");
            if (regulationType == null) {
                regulationType = "알 수 없음";
            }

            String header = String.format("[%s, 관련도: %.2f]\n", regulationType, score);

            // 현재까지의 길이 체크
            if (currentLength + header.length() + text.length() > maxLength) {
                // 남은 공간 계산
                int remaining = maxLength - currentLength - header.length();
                if (remaining > 100) {
                    // 충분한 공간이 있으면 잘라서 추가
                    context.append(header);
                    context.append(text.substring(0, remaining - 3)).append("...\n");
                }
                break;
            }

            context.append(header);
            context.append(text).append("\n\n");
            currentLength = context.length();
        }

        return context.toString();
    }

    /**
     * 메타데이터가 풍부한 컨텍스트 생성
     *
     * @param segments 검색된 세그먼트 목록
     * @param includeScores 점수 포함 여부
     * @param includeArticles 조항 번호 포함 여부
     * @return 메타데이터 포함 컨텍스트
     */
    public static String buildDetailedContext(
            List<EmbeddingMatch<TextSegment>> segments,
            boolean includeScores,
            boolean includeArticles) {

        if (segments == null || segments.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            EmbeddingMatch<TextSegment> match = segments.get(i);
            TextSegment segment = match.embedded();
            double score = match.score();

            String regulationType = segment.metadata().getString("regulation_type");
            String text = segment.text();

            // 헤더 구성
            StringBuilder header = new StringBuilder();
            header.append(String.format("【%s】", regulationType != null ? regulationType : "규정"));

            if (includeArticles) {
                String firstArticle = RegulationArticleExtractor.extractFirstArticleNumber(text);
                if (firstArticle != null) {
                    header.append(" ").append(firstArticle);
                }
            }

            if (includeScores) {
                header.append(String.format(" [관련도: %.1f%%]", score * 100));
            }

            context.append(header).append("\n");
            context.append(text).append("\n\n");
        }

        return context.toString();
    }

    /**
     * 조항별로 그룹화된 컨텍스트 생성
     *
     * @param segments 검색된 세그먼트 목록
     * @return 조항별 그룹화된 컨텍스트
     */
    public static String buildArticleGroupedContext(List<EmbeddingMatch<TextSegment>> segments) {
        if (segments == null || segments.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        // 조항이 있는 세그먼트와 없는 세그먼트 분리
        List<EmbeddingMatch<TextSegment>> withArticles = segments.stream()
                .filter(s -> RegulationArticleExtractor.containsArticle(s.embedded().text()))
                .collect(Collectors.toList());

        List<EmbeddingMatch<TextSegment>> withoutArticles = segments.stream()
                .filter(s -> !RegulationArticleExtractor.containsArticle(s.embedded().text()))
                .collect(Collectors.toList());

        // 조항이 있는 것들 먼저 출력
        if (!withArticles.isEmpty()) {
            context.append("=== 관련 조항 ===\n\n");
            for (EmbeddingMatch<TextSegment> match : withArticles) {
                String regulationType = match.embedded().metadata().getString("regulation_type");
                String text = match.embedded().text();

                context.append(String.format("【%s】\n", regulationType != null ? regulationType : "규정"));
                context.append(text).append("\n\n");
            }
        }

        // 조항이 없는 일반 내용
        if (!withoutArticles.isEmpty()) {
            context.append("=== 관련 내용 ===\n\n");
            for (EmbeddingMatch<TextSegment> match : withoutArticles) {
                context.append(match.embedded().text()).append("\n\n");
            }
        }

        return context.toString();
    }
}
