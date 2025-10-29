package com.guideon.analyzer;

import com.guideon.analyzer.DictionaryLoader;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanReadingFormFilter;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 검색 쿼리 전용 Analyzer (Phase 4.1 + 4.2)
 *
 * EnhancedKoreanAnalyzer보다 더 완화된 필터링
 * - 불용어 제거를 최소화하여 검색 재현율 향상
 * - 사용자 사전은 동일하게 적용
 * - 동의어 확장 적용 (Phase 4.2)
 */
public class SearchQueryAnalyzer extends Analyzer {
    private static final Logger logger = LoggerFactory.getLogger(SearchQueryAnalyzer.class);

    private final UserDictionary userDictionary;
    private final SynonymMap synonymMap;  // Phase 4.2
    private final KoreanTokenizer.DecompoundMode decompoundMode;

    /**
     * 기본 생성자
     */
    public SearchQueryAnalyzer() {
        this(
            DictionaryLoader.loadUserDictionary(),
            DictionaryLoader.loadSynonyms(),  // Phase 4.2
            KoreanTokenizer.DecompoundMode.MIXED
        );
    }

    /**
     * 커스텀 생성자
     */
    public SearchQueryAnalyzer(
            UserDictionary userDictionary,
            SynonymMap synonymMap,
            KoreanTokenizer.DecompoundMode decompoundMode) {
        super();  // Lucene Analyzer 초기화

        this.userDictionary = userDictionary;
        this.synonymMap = synonymMap;  // Phase 4.2
        this.decompoundMode = decompoundMode;

        logger.info("SearchQueryAnalyzer initialized (Phase 4.1 + 4.2):");
        logger.info("  - User Dictionary: {}", userDictionary != null ? "Loaded" : "Not loaded");
        logger.info("  - Synonyms: {}", synonymMap != null ? "Enabled" : "Disabled");
        logger.info("  - Decompound Mode: {}", decompoundMode);
        logger.info("  - Stop Words: Minimal (only POS-based)");
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // 1. Tokenizer: 한국어 형태소 분석 (인덱싱과 동일)
        KoreanTokenizer tokenizer = new KoreanTokenizer(
            org.apache.lucene.util.AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
            userDictionary,
            decompoundMode,
            false
        );

        // 2. Token Filter Chain (검색용 - 최소 필터링)
        TokenStream stream = tokenizer;

        // 2.1 품사 기반 불용어 제거 (필수: 조사, 기호만 제거)
        stream = new KoreanPartOfSpeechStopFilter(
            stream,
            createMinimalPOSStopTags()
        );

        // 2.2 한자 읽기 변환
        stream = new KoreanReadingFormFilter(stream);

        // 2.3 일반 불용어 제거 생략 (검색 재현율 향상)
        // stream = new StopFilter(stream, stopWords); // ← 검색 시에는 생략

        // 2.4 소문자 변환 (영문 처리용)
        stream = new LowerCaseFilter(stream);

        // 2.5 동의어 확장 (Phase 4.2)
        if (synonymMap != null) {
            stream = new SynonymGraphFilter(stream, synonymMap, true);
        }

        // 2.6 토큰 길이 필터링
        stream = new LengthFilter(stream, 1, 50);

        return new TokenStreamComponents(tokenizer, stream);
    }

    /**
     * 검색용 최소 품사 필터 (조사와 기호만 제거)
     * 어미는 검색 재현율을 위해 유지
     */
    private Set<POS.Tag> createMinimalPOSStopTags() {
        return Set.of(
            POS.Tag.J,      // 조사만 제거
            POS.Tag.SF,     // 구두점
            POS.Tag.SE      // 줄임표
            // POS.Tag.E는 제거하지 않음 (어미 유지)
        );
    }
}
