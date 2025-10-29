package com.guideon.util;

import com.guideon.model.RegulationArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 규정 조항 추출 유틸리티
 * 텍스트에서 "제XX조", "제XX항" 등의 조항 번호를 추출하고 파싱
 */
public class RegulationArticleExtractor {
    private static final Logger logger = LoggerFactory.getLogger(RegulationArticleExtractor.class);

    // 조항 번호 패턴
    private static final Pattern ARTICLE_PATTERN = Pattern.compile(
            "제\\s*(\\d+)\\s*조(?:\\s*\\(([^)]+)\\))?"  // 제XX조 (제목)
    );

    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "제\\s*(\\d+)\\s*항"  // 제XX항
    );

    private static final Pattern SUBITEM_PATTERN = Pattern.compile(
            "제\\s*(\\d+)\\s*호"  // 제XX호
    );

    /**
     * 텍스트에서 모든 조항 번호 추출
     *
     * @param text 텍스트
     * @param regulationType 규정 유형
     * @return 추출된 조항 목록
     */
    public static List<RegulationArticle> extractArticles(String text, String regulationType) {
        List<RegulationArticle> articles = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return articles;
        }

        Matcher matcher = ARTICLE_PATTERN.matcher(text);

        while (matcher.find()) {
            String number = matcher.group(1);
            String title = matcher.group(2);

            String articleNumber = "제" + number + "조";

            // 조항 내용 추출 시도
            String content = extractArticleContent(text, matcher.start());

            RegulationArticle article = new RegulationArticle(
                    articleNumber,
                    title != null ? title : "",
                    content,
                    regulationType
            );

            articles.add(article);
        }

        logger.debug("Extracted {} articles from text (length: {})", articles.size(), text.length());

        return articles;
    }

    /**
     * 조항 내용 추출 (조항 번호 이후 다음 조항까지)
     *
     * @param text 전체 텍스트
     * @param startPos 조항 번호 시작 위치
     * @return 조항 내용
     */
    private static String extractArticleContent(String text, int startPos) {
        // 현재 조항부터 다음 조항까지 추출
        int nextArticlePos = findNextArticlePosition(text, startPos + 1);

        if (nextArticlePos == -1) {
            // 마지막 조항인 경우
            String content = text.substring(startPos);
            return cleanContent(content);
        } else {
            String content = text.substring(startPos, nextArticlePos);
            return cleanContent(content);
        }
    }

    /**
     * 다음 조항 시작 위치 찾기
     *
     * @param text 텍스트
     * @param fromPos 검색 시작 위치
     * @return 다음 조항 위치 (없으면 -1)
     */
    private static int findNextArticlePosition(String text, int fromPos) {
        Matcher matcher = ARTICLE_PATTERN.matcher(text);
        if (matcher.find(fromPos)) {
            return matcher.start();
        }
        return -1;
    }

    /**
     * 조항 내용 정리
     *
     * @param content 원본 내용
     * @return 정리된 내용
     */
    private static String cleanContent(String content) {
        if (content == null) {
            return "";
        }

        // 앞뒤 공백 제거
        content = content.trim();

        // 연속된 공백을 하나로
        content = content.replaceAll("\\s+", " ");

        // 너무 긴 내용은 잘라내기 (500자 제한)
        if (content.length() > 500) {
            content = content.substring(0, 497) + "...";
        }

        return content;
    }

    /**
     * 텍스트에 조항 번호가 포함되어 있는지 확인
     *
     * @param text 텍스트
     * @return 조항 포함 여부
     */
    public static boolean containsArticle(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        Matcher matcher = ARTICLE_PATTERN.matcher(text);
        return matcher.find();
    }

    /**
     * 첫 번째 조항 번호만 추출
     *
     * @param text 텍스트
     * @return 첫 번째 조항 번호 (없으면 null)
     */
    public static String extractFirstArticleNumber(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        Matcher matcher = ARTICLE_PATTERN.matcher(text);
        if (matcher.find()) {
            String number = matcher.group(1);
            return "제" + number + "조";
        }

        return null;
    }

    /**
     * 모든 조항 번호만 추출 (제목 제외)
     *
     * @param text 텍스트
     * @return 조항 번호 목록
     */
    public static List<String> extractArticleNumbers(String text) {
        List<String> numbers = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return numbers;
        }

        Matcher matcher = ARTICLE_PATTERN.matcher(text);

        while (matcher.find()) {
            String number = matcher.group(1);
            numbers.add("제" + number + "조");
        }

        return numbers;
    }

    /**
     * 조항 정보를 포함한 텍스트인지 확인 (품질 체크)
     *
     * @param text 텍스트
     * @return 조항 정보 포함 여부
     */
    public static boolean hasArticleStructure(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // "제XX조" 패턴이 있는지 확인
        Matcher articleMatcher = ARTICLE_PATTERN.matcher(text);
        if (articleMatcher.find()) {
            return true;
        }

        // "제XX항" 패턴이 있는지 확인
        Matcher itemMatcher = ITEM_PATTERN.matcher(text);
        if (itemMatcher.find()) {
            return true;
        }

        return false;
    }
}
