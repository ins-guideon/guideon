package com.guideon.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4.1: 사용자 사전, 불용어 로더
 *
 * 한국어 분석기 튜닝을 위한 사전 파일 로더
 */
public class DictionaryLoader {
    private static final Logger logger = LoggerFactory.getLogger(DictionaryLoader.class);

    private static final String USER_DICT_PATH = "korean-dictionary/user-dict.txt";
    private static final String STOPWORDS_PATH = "korean-dictionary/stopwords.txt";
    private static final String SYNONYMS_PATH = "korean-dictionary/synonyms.txt";

    /**
     * 사용자 사전 로드
     * Nori Analyzer가 인식할 수 있는 도메인 특화 용어 사전
     *
     * @return UserDictionary 객체, 실패 시 null
     */
    public static UserDictionary loadUserDictionary() {
        logger.info("Loading user dictionary from: {}", USER_DICT_PATH);

        try (InputStream is = getResourceAsStream(USER_DICT_PATH);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            UserDictionary dict = UserDictionary.open(reader);
            logger.info("✓ User dictionary loaded successfully");
            return dict;

        } catch (IOException e) {
            logger.warn("Failed to load user dictionary, using default Nori dictionary", e);
            return null;
        }
    }

    /**
     * 동의어 사전 로드 (Phase 4.2)
     * 동의어 확장으로 검색 재현율 향상
     *
     * @return SynonymMap 동의어 맵, 실패 시 null
     */
    public static SynonymMap loadSynonyms() {
        logger.info("Loading synonyms from: {}", SYNONYMS_PATH);

        try (InputStream is = getResourceAsStream(SYNONYMS_PATH);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             Analyzer analyzer = new WhitespaceAnalyzer()) {

            // Solr 형식의 동의어 파서 사용
            // dedup=true: 중복 동의어 제거, expand=true: 양방향 확장
            // analyzer: WhitespaceAnalyzer 사용 (공백으로 토큰 분리)
            SolrSynonymParser parser = new SolrSynonymParser(true, true, analyzer);
            parser.parse(reader);

            SynonymMap synonymMap = parser.build();
            logger.info("✓ Synonym dictionary loaded successfully");
            return synonymMap;

        } catch (IOException | java.text.ParseException e) {
            logger.info("Synonym dictionary not found or failed to load, synonyms disabled", e);
            return null;
        }
    }

    /**
     * 불용어 사전 로드
     * 검색 품질 향상을 위해 의미 없는 조사, 접속사 등을 제거
     *
     * @return CharArraySet 불용어 집합
     */
    public static CharArraySet loadStopWords() {
        logger.info("Loading stop words from: {}", STOPWORDS_PATH);

        List<String> stopWords = new ArrayList<>();

        // 기본 불용어 (파일이 없어도 기본 제공)
        stopWords.addAll(getDefaultStopWords());

        // 파일에서 추가 불용어 로드
        try (InputStream is = getResourceAsStream(STOPWORDS_PATH);
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 빈 줄이나 주석(#)은 무시
                if (!line.isEmpty() && !line.startsWith("#")) {
                    stopWords.add(line);
                    count++;
                }
            }

            logger.info("✓ Loaded {} stop words from file (total: {})", count, stopWords.size());

        } catch (IOException e) {
            logger.info("Stop words file not found, using default stop words only ({})",
                stopWords.size());
        }

        // CharArraySet 생성 (대소문자 무시)
        return new CharArraySet(stopWords, true);
    }

    /**
     * 기본 불용어 리스트
     * 파일이 없어도 최소한의 불용어는 제공
     */
    private static List<String> getDefaultStopWords() {
        List<String> defaults = new ArrayList<>();

        // 조사
        defaults.add("은");
        defaults.add("는");
        defaults.add("이");
        defaults.add("가");
        defaults.add("을");
        defaults.add("를");
        defaults.add("에");
        defaults.add("에서");
        defaults.add("의");
        defaults.add("로");
        defaults.add("으로");
        defaults.add("와");
        defaults.add("과");
        defaults.add("도");
        defaults.add("만");
        defaults.add("부터");
        defaults.add("까지");
        defaults.add("한테");
        defaults.add("께");

        // 접속사
        defaults.add("그리고");
        defaults.add("그러나");
        defaults.add("하지만");
        defaults.add("또는");
        defaults.add("및");
        defaults.add("또한");
        defaults.add("그래서");

        return defaults;
    }

    /**
     * 리소스 파일을 InputStream으로 로드
     *
     * @param path 리소스 경로
     * @return InputStream
     * @throws IOException 파일을 찾을 수 없을 때
     */
    private static InputStream getResourceAsStream(String path) throws IOException {
        // ClassLoader를 통해 리소스 로드
        InputStream is = DictionaryLoader.class.getClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new IOException("Resource not found: " + path);
        }

        return is;
    }

    /**
     * 사전 로딩 테스트 메서드
     */
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("Testing Dictionary Loader");
        logger.info("========================================");

        // 사용자 사전 테스트
        UserDictionary userDict = loadUserDictionary();
        if (userDict != null) {
            logger.info("✓ User dictionary loaded successfully");
        } else {
            logger.warn("✗ User dictionary loading failed");
        }

        // 불용어 테스트
        CharArraySet stopWords = loadStopWords();
        logger.info("✓ Stop words loaded: {} words", stopWords.size());

        // 샘플 출력
        logger.info("\nSample stop words:");
        int count = 0;
        for (Object word : stopWords) {
            if (count++ >= 10) break;
            logger.info("  - {}", word);
        }

        logger.info("========================================");
        logger.info("Dictionary Loader Test Complete");
        logger.info("========================================");
    }
}
