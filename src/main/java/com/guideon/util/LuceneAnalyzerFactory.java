package com.guideon.util;

import com.guideon.analyzer.EnhancedKoreanAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Lucene Analyzer 팩토리
 * 한국어 Nori Analyzer 생성 및 설정
 */
public class LuceneAnalyzerFactory {
    private static final Logger logger = LoggerFactory.getLogger(LuceneAnalyzerFactory.class);

    /**
     * 한국어 Nori Analyzer 생성 (기본 설정)
     * Phase 4.1: EnhancedKoreanAnalyzer 사용
     *
     * @return 한국어 형태소 분석기
     */
    public static Analyzer createKoreanAnalyzer() {
        logger.info("Creating Enhanced Korean Analyzer (Phase 4.1)");
        return new EnhancedKoreanAnalyzer();
    }

    /**
     * 한국어 Nori Analyzer 생성 (커스터마이징)
     *
     * @param decompoundMode 복합어 분해 모드
     * @return 한국어 형태소 분석기
     */
    public static Analyzer createKoreanAnalyzer(KoreanTokenizer.DecompoundMode decompoundMode) {
        logger.info("Creating Enhanced Korean Analyzer with mode: {}", decompoundMode);
        return new EnhancedKoreanAnalyzer(null, null, decompoundMode);
    }

    /**
     * 레거시 기본 한국어 Analyzer (Phase 4 이전 버전)
     * 테스트 및 비교용
     *
     * @return 기본 KoreanAnalyzer
     */
    @Deprecated
    public static Analyzer createBasicKoreanAnalyzer() {
        logger.warn("Using basic KoreanAnalyzer (deprecated, use createKoreanAnalyzer instead)");
        return new KoreanAnalyzer();
    }

    /**
     * 검색 쿼리 전용 Analyzer 생성 (Phase 4.1)
     * 최소 필터링으로 검색 재현율 향상
     *
     * @return SearchQueryAnalyzer
     */
    public static Analyzer createSearchQueryAnalyzer() {
        logger.info("Creating Search Query Analyzer (Phase 4.1 - minimal filtering)");
        return new com.guideon.analyzer.SearchQueryAnalyzer();
    }

    /**
     * 표준 영어 Analyzer 생성
     *
     * @return 표준 Analyzer
     */
    public static Analyzer createStandardAnalyzer() {
        return new StandardAnalyzer();
    }

    /**
     * 한국어 불용어 리스트 로드
     * 검색 품질을 높이기 위해 의미없는 조사, 접속사 등 제거
     *
     * @return 불용어 집합
     */
    private static CharArraySet loadKoreanStopWords() {
        List<String> stopWords = Arrays.asList(
                // 조사
                "은", "는", "이", "가", "을", "를", "에", "에서", "의", "로", "으로",
                "와", "과", "도", "만", "부터", "까지", "에게", "한테", "께",

                // 접속사
                "그리고", "그러나", "하지만", "또는", "및", "또한", "그래서",

                // 대명사
                "저", "나", "너", "우리", "저희", "그", "이", "그것", "이것", "저것",

                // 기타
                "있다", "없다", "되다", "하다", "이다", "아니다"
        );

        return new CharArraySet(stopWords, true); // true = ignore case
    }

    /**
     * Decompound Mode 문자열을 enum으로 변환
     *
     * @param mode "none", "discard", "mixed"
     * @return DecompoundMode enum
     */
    public static KoreanTokenizer.DecompoundMode parseDecompoundMode(String mode) {
        switch (mode.toLowerCase()) {
            case "none":
                return KoreanTokenizer.DecompoundMode.NONE;
            case "discard":
                return KoreanTokenizer.DecompoundMode.DISCARD;
            case "mixed":
            default:
                return KoreanTokenizer.DecompoundMode.MIXED;
        }
    }
}
