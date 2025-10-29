package com.guideon.analyzer;

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
 * Phase 4.1 + 4.2: 규정 검색에 최적화된 한국어 Analyzer
 *
 * 주요 기능:
 * 1. 사용자 사전 적용 (규정 용어) - Phase 4.1
 * 2. 품사 기반 불용어 제거 (조사, 어미) - Phase 4.1
 * 3. 일반 불용어 제거 - Phase 4.1
 * 4. 한자 읽기 변환 - Phase 4.1
 * 5. 동의어 확장 (Synonym expansion) - Phase 4.2
 * 6. 토큰 길이 필터링
 */
public class EnhancedKoreanAnalyzer extends Analyzer {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedKoreanAnalyzer.class);

    private final UserDictionary userDictionary;
    private final CharArraySet stopWords;
    private final SynonymMap synonymMap;  // Phase 4.2
    private final KoreanTokenizer.DecompoundMode decompoundMode;

    /**
     * 기본 생성자 - DictionaryLoader로부터 사전 자동 로드
     */
    public EnhancedKoreanAnalyzer() {
        this(
            DictionaryLoader.loadUserDictionary(),
            DictionaryLoader.loadStopWords(),
            DictionaryLoader.loadSynonyms(),  // Phase 4.2
            KoreanTokenizer.DecompoundMode.MIXED
        );
    }

    /**
     * 커스텀 생성자
     *
     * @param userDictionary 사용자 사전 (null 가능)
     * @param stopWords 불용어 집합 (null 가능)
     * @param synonymMap 동의어 맵 (null 가능) - Phase 4.2
     * @param decompoundMode 복합어 분해 모드
     */
    public EnhancedKoreanAnalyzer(
            UserDictionary userDictionary,
            CharArraySet stopWords,
            SynonymMap synonymMap,
            KoreanTokenizer.DecompoundMode decompoundMode) {
        super();  // Lucene Analyzer 초기화

        this.userDictionary = userDictionary;
        this.stopWords = stopWords;
        this.synonymMap = synonymMap;  // Phase 4.2
        this.decompoundMode = decompoundMode;

        logger.info("EnhancedKoreanAnalyzer initialized (Phase 4.1 + 4.2):");
        logger.info("  - User Dictionary: {}", userDictionary != null ? "Loaded" : "Not loaded");
        logger.info("  - Stop Words: {} words", stopWords != null ? stopWords.size() : 0);
        logger.info("  - Synonyms: {}", synonymMap != null ? "Enabled" : "Disabled");
        logger.info("  - Decompound Mode: {}", decompoundMode);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // 1. Tokenizer: 한국어 형태소 분석
        // AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY 사용
        KoreanTokenizer tokenizer = new KoreanTokenizer(
            org.apache.lucene.util.AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
            userDictionary,          // 사용자 사전
            decompoundMode,          // 복합어 분해 모드
            false                    // outputUnknownUnigrams (false = 미등록어를 유니그램으로 출력하지 않음)
        );

        // 2. Token Filter Chain 구성
        TokenStream stream = tokenizer;

        // 2.1 품사 기반 불용어 제거 (조사, 어미, 기호 등)
        stream = new KoreanPartOfSpeechStopFilter(
            stream,
            createPOSStopTags()
        );

        // 2.2 한자 읽기 변환 (漢字 → 한자)
        stream = new KoreanReadingFormFilter(stream);

        // 2.3 일반 불용어 제거
        if (stopWords != null && !stopWords.isEmpty()) {
            stream = new StopFilter(stream, stopWords);
        }

        // 2.4 소문자 변환 (영문 처리용)
        stream = new LowerCaseFilter(stream);

        // 2.5 동의어 확장 (Phase 4.2)
        // 주의: SynonymGraphFilter는 소문자 변환 이후에 적용
        if (synonymMap != null) {
            stream = new SynonymGraphFilter(stream, synonymMap, true);
        }

        // 2.6 너무 짧거나 긴 토큰 제거
        // 최소 1글자, 최대 50글자
        stream = new LengthFilter(stream, 1, 50);

        return new TokenStreamComponents(tokenizer, stream);
    }

    /**
     * 제거할 품사 태그 정의
     *
     * Phase 4.1에서는 기본적인 품사만 제거
     * (조사, 어미, 기호)
     */
    private Set<POS.Tag> createPOSStopTags() {
        return Set.of(
            POS.Tag.J,      // 조사 (Postposition)
                           // 예: 은, 는, 이, 가, 을, 를, 에, 에서, 의, 로

            POS.Tag.E,      // 어미 (Verb/Adjective Ending)
                           // 예: 다, 요, 습니다, 입니다

            POS.Tag.SF,     // 구두점 (Punctuation)
                           // 예: 마침표, 쉼표, 물음표, 느낌표

            POS.Tag.SE      // 줄임표 (Ellipsis)
                           // 예: ...
        );
    }

    /**
     * Analyzer 정보 출력 (디버깅용)
     */
    public String getInfo() {
        return String.format(
            "EnhancedKoreanAnalyzer[userDict=%s, stopWords=%d, mode=%s]",
            userDictionary != null ? "enabled" : "disabled",
            stopWords != null ? stopWords.size() : 0,
            decompoundMode
        );
    }

    /**
     * DecompoundMode 설명:
     *
     * NONE: 복합어를 분리하지 않음
     *   - "해외출장비" → [해외출장비]
     *   - 장점: 빠름, 정확한 매칭
     *   - 단점: 재현율 낮음
     *
     * DISCARD: 원본 폐기, 분리된 토큰만 유지
     *   - "해외출장비" → [해외, 출장, 비]
     *   - 장점: 재현율 향상
     *   - 단점: 정확도 저하 가능
     *
     * MIXED: 원본 + 분리 토큰 모두 유지 (권장)
     *   - "해외출장비" → [해외출장비, 해외, 출장, 비]
     *   - 장점: 정확도와 재현율 모두 높음
     *   - 단점: 인덱스 크기 증가
     */
}
