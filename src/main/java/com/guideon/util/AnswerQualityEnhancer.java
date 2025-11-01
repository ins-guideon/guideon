package com.guideon.util;

import com.guideon.model.QueryAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 답변 품질 향상을 위한 유틸리티 클래스
 * - 답변 검증 및 품질 점수 계산
 * - 답변 후처리 및 포맷팅
 * - 의도별 답변 개선
 */
public class AnswerQualityEnhancer {

    private static final Logger logger = LoggerFactory.getLogger(AnswerQualityEnhancer.class);

    // 답변 품질 평가 기준
    private static final int MIN_ANSWER_LENGTH = 50;
    private static final int OPTIMAL_ANSWER_LENGTH = 200;
    private static final Pattern ARTICLE_REFERENCE_PATTERN = Pattern.compile("제\\s*\\d+\\s*조");
    private static final Pattern NEGATIVE_PATTERNS = Pattern.compile(
        "(죄송|모르겠|찾을 수 없|관련.*없|정보.*부족|불충분|확인.*어렵)"
    );

    /**
     * 답변 품질 점수 계산 (0.0 ~ 1.0)
     */
    public static double calculateAnswerQualityScore(String answer, QueryAnalysisResult analysis) {
        logger.debug("Calculating answer quality score for: {}", answer.substring(0, Math.min(50, answer.length())));

        double score = 0.0;

        // 1. 답변 길이 평가 (0.25)
        score += evaluateLength(answer);

        // 2. 규정 조항 참조 평가 (0.25)
        score += evaluateArticleReferences(answer);

        // 3. 부정적 표현 체크 (0.25)
        score += evaluatePositivity(answer);

        // 4. 구조화 및 가독성 평가 (0.25)
        score += evaluateStructure(answer);

        logger.info("Answer quality score: {:.3f} (length: {:.2f}, refs: {:.2f}, positive: {:.2f}, structure: {:.2f})",
            score,
            evaluateLength(answer),
            evaluateArticleReferences(answer),
            evaluatePositivity(answer),
            evaluateStructure(answer));

        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * 답변 길이 평가
     */
    private static double evaluateLength(String answer) {
        int length = answer.length();

        if (length < MIN_ANSWER_LENGTH) {
            return 0.1; // 너무 짧음
        } else if (length < OPTIMAL_ANSWER_LENGTH) {
            return 0.15 + (0.1 * (length - MIN_ANSWER_LENGTH) / (OPTIMAL_ANSWER_LENGTH - MIN_ANSWER_LENGTH));
        } else {
            return 0.25; // 최적 길이 이상
        }
    }

    /**
     * 규정 조항 참조 평가
     */
    private static double evaluateArticleReferences(String answer) {
        Matcher matcher = ARTICLE_REFERENCE_PATTERN.matcher(answer);
        int referenceCount = 0;

        while (matcher.find()) {
            referenceCount++;
        }

        if (referenceCount == 0) {
            return 0.05; // 조항 참조 없음
        } else if (referenceCount <= 2) {
            return 0.15; // 적절한 참조
        } else {
            return 0.25; // 충분한 참조
        }
    }

    /**
     * 부정적 표현 체크
     */
    private static double evaluatePositivity(String answer) {
        Matcher matcher = NEGATIVE_PATTERNS.matcher(answer);

        if (matcher.find()) {
            return 0.05; // 부정적 표현 포함
        } else {
            return 0.25; // 긍정적/명확한 답변
        }
    }

    /**
     * 구조화 및 가독성 평가
     */
    private static double evaluateStructure(String answer) {
        double score = 0.0;

        // 번호 매김 또는 불릿 포인트 사용
        if (answer.matches("(?s).*[1-9]\\.|.*[-•].*")) {
            score += 0.10;
        }

        // 단락 구분 (줄바꿈)
        int paragraphs = answer.split("\n\n").length;
        if (paragraphs >= 2) {
            score += 0.10;
        }

        // 적절한 문장 수 (3개 이상)
        int sentences = answer.split("[.!?]").length;
        if (sentences >= 3) {
            score += 0.05;
        }

        return Math.min(0.25, score);
    }

    /**
     * 답변 후처리 및 Markdown 포맷팅
     */
    public static String enhanceAnswer(String rawAnswer, QueryAnalysisResult analysis, List<String> extractedArticles) {
        logger.debug("Enhancing answer with Markdown formatting...");

        StringBuilder enhanced = new StringBuilder();

        // 1. 답변 내용 정리 및 Markdown 변환
        String cleanedAnswer = cleanAnswer(rawAnswer);
        String markdownAnswer = convertToMarkdown(cleanedAnswer);
        enhanced.append(markdownAnswer);

        // 2. 참조 조항 섹션 추가 (Markdown 형식)
        if (extractedArticles != null && !extractedArticles.isEmpty()) {
            enhanced.append("\n\n---\n\n");
            enhanced.append("### 📋 참조 조항\n\n");
            for (String article : extractedArticles) {
                enhanced.append("- **").append(article).append("**\n");
            }
        }

        // 3. 의도별 추가 정보 (Markdown 인용구 형식)
        if (analysis != null) {
            String intentEnhancement = addIntentBasedEnhancement(cleanedAnswer, analysis.getIntent());
            if (!intentEnhancement.isEmpty()) {
                enhanced.append("\n\n---\n\n");
                enhanced.append("> 💡 **추가 안내**\n");
                enhanced.append("> \n");
                enhanced.append("> ").append(intentEnhancement);
            }
        }

        String result = enhanced.toString();
        logger.info("Answer enhanced with Markdown: original length={}, enhanced length={}", rawAnswer.length(), result.length());

        return result;
    }

    /**
     * 답변을 Markdown 형식으로 변환
     */
    private static String convertToMarkdown(String answer) {
        String markdown = answer;

        // 1. 번호 매김 리스트 개선
        markdown = markdown.replaceAll("(?m)^(\\d+)\\.", "**$1.**");

        // 2. 조항 참조를 굵게 표시
        markdown = markdown.replaceAll("(제\\s*\\d+\\s*조)", "**$1**");

        // 3. 규정 유형을 굵게 표시 (예: "취업규칙", "근태관리규정" 등)
        markdown = markdown.replaceAll("(취업규칙|근태관리규정|복리후생비규정|출장여비규정|급여규정)(\\s+제)", "**$1**$2");

        // 4. "단," 또는 "다만," 으로 시작하는 예외사항을 강조
        markdown = markdown.replaceAll("(?m)^(단,|다만,)", "> **$1**");

        // 5. 금액이나 숫자를 강조 (예: 15일, 20만원)
        markdown = markdown.replaceAll("(\\d+(?:,\\d{3})*(?:일|개월|년|원|%|시간))", "**$1**");

        return markdown;
    }

    /**
     * 답변 정리 (불필요한 공백, 중복 제거)
     */
    private static String cleanAnswer(String answer) {
        return answer
            .replaceAll("\n{3,}", "\n\n")  // 과도한 줄바꿈 제거
            .replaceAll(" {2,}", " ")       // 과도한 공백 제거
            .trim();
    }

    /**
     * 의도별 추가 정보 제공
     */
    private static String addIntentBasedEnhancement(String answer, String intent) {
        if (intent == null) {
            return "";
        }

        return switch (intent) {
            case "기준확인" -> {
                // 기준 확인 질문에는 추가 안내 제공
                if (!answer.contains("문의") && !answer.contains("확인")) {
                    yield "💡 추가 정보가 필요하시면 인사팀에 문의하시기 바랍니다.";
                }
                yield "";
            }
            case "절차설명" -> {
                // 절차 설명에는 관련 부서 안내
                if (!answer.contains("부서") && !answer.contains("담당")) {
                    yield "💡 구체적인 절차는 해당 부서 담당자에게 문의하시면 더 자세한 안내를 받으실 수 있습니다.";
                }
                yield "";
            }
            case "가능여부" -> {
                // 가능여부 질문에는 예외사항 안내
                if (!answer.contains("예외") && !answer.contains("단,")) {
                    yield "💡 개별 상황에 따라 예외가 있을 수 있으니, 정확한 판단은 인사팀과 상담하시기 바랍니다.";
                }
                yield "";
            }
            default -> "";
        };
    }

    /**
     * 답변 검증 (최소 품질 기준 충족 여부)
     */
    public static boolean validateAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            logger.warn("Answer validation failed: empty answer");
            return false;
        }

        if (answer.length() < MIN_ANSWER_LENGTH) {
            logger.warn("Answer validation failed: too short (length={})", answer.length());
            return false;
        }

        // 부정적 답변 체크
        Matcher negativeMatcher = NEGATIVE_PATTERNS.matcher(answer);
        if (negativeMatcher.find()) {
            logger.warn("Answer validation warning: contains negative pattern");
            // 부정적이어도 일단 통과 (품질 점수에만 영향)
        }

        logger.info("Answer validation passed");
        return true;
    }

    /**
     * 답변에서 참조된 조항 추출
     */
    public static List<String> extractReferencedArticles(String answer) {
        List<String> articles = new ArrayList<>();
        Matcher matcher = ARTICLE_REFERENCE_PATTERN.matcher(answer);

        while (matcher.find()) {
            String article = matcher.group().replaceAll("\\s+", " ").trim();
            if (!articles.contains(article)) {
                articles.add(article);
            }
        }

        logger.debug("Extracted {} article references from answer", articles.size());
        return articles;
    }

    /**
     * 신뢰도 기반 답변 보강 (개선된 버전)
     */
    public static String addConfidenceIndicator(String answer, double confidenceScore) {
        StringBuilder result = new StringBuilder(answer);

        // 신뢰도에 따라 안내 메시지 추가
        if (confidenceScore >= 0.85) {
            // 매우 높은 신뢰도 (85% 이상) - 긍정적 메시지
            result.append("\n\n✅ **신뢰도: 높음** - 규정 내용과 높은 관련성이 확인되었습니다.");
        } else if (confidenceScore >= 0.7) {
            // 높은 신뢰도 (70~85%) - 메시지 없음 (기본)
            // 충분히 신뢰할 수 있는 수준이므로 별도 메시지 불필요
        } else if (confidenceScore >= 0.5) {
            // 중간 신뢰도 (50~70%) - 확인 권장
            result.append("\n\n💡 **참고**: 추가 확인이 필요하시면 인사팀에 문의하시기 바랍니다.");
        } else if (confidenceScore >= 0.3) {
            // 낮은 신뢰도 (30~50%) - 주의 필요
            result.append("\n\n⚠️ **주의**: 검색 결과의 관련도가 낮을 수 있습니다. 정확한 정보는 인사팀에 확인하시기 바랍니다.");
        } else {
            // 매우 낮은 신뢰도 (30% 미만) - 강한 경고
            result.append("\n\n🚨 **경고**: 관련 규정을 찾기 어렵습니다. 반드시 인사팀에 문의하여 정확한 정보를 확인하시기 바랍니다.");
        }

        return result.toString();
    }
}
