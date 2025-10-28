package com.guideon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * application.properties 파일을 로드하는 유틸리티 클래스
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String DEFAULT_CONFIG_FILE = "application.properties";

    private Properties properties;

    public ConfigLoader() {
        this.properties = new Properties();
        loadDefaultConfig();
    }

    public ConfigLoader(String configFilePath) {
        this.properties = new Properties();
        loadConfig(configFilePath);
    }

    /**
     * 기본 설정 파일 로드 (classpath의 application.properties)
     */
    private void loadDefaultConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (input == null) {
                logger.warn("Unable to find {}, using default values", DEFAULT_CONFIG_FILE);
                loadEnvironmentVariables();
                return;
            }
            properties.load(input);
            logger.info("Configuration loaded from classpath: {}", DEFAULT_CONFIG_FILE);

            // 환경변수로 오버라이드
            overrideWithEnvironmentVariables();

        } catch (IOException e) {
            logger.error("Error loading configuration file", e);
            loadEnvironmentVariables();
        }
    }

    /**
     * 지정된 경로의 설정 파일 로드
     */
    private void loadConfig(String configFilePath) {
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            logger.info("Configuration loaded from: {}", configFilePath);

            // 환경변수로 오버라이드
            overrideWithEnvironmentVariables();

        } catch (IOException e) {
            logger.error("Error loading configuration file from: {}", configFilePath, e);
            loadDefaultConfig();
        }
    }

    /**
     * 환경변수로 설정값 오버라이드
     */
    private void overrideWithEnvironmentVariables() {
        String envApiKey = System.getenv("GOOGLE_API_KEY");
        if (envApiKey != null && !envApiKey.isEmpty()) {
            properties.setProperty("gemini.api.key", envApiKey);
            logger.info("Gemini API key overridden by environment variable");
        }

        String cohereApiKey = System.getenv("COHERE_API_KEY");
        if (cohereApiKey != null && !cohereApiKey.isEmpty()) {
            properties.setProperty("cohere.api.key", cohereApiKey);
            logger.info("Cohere API key overridden by environment variable");
        }
    }

    /**
     * 환경변수에서 설정 로드 (폴백)
     */
    private void loadEnvironmentVariables() {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            properties.setProperty("gemini.api.key", apiKey);
            logger.info("Using GOOGLE_API_KEY from environment variable");
        }
    }

    /**
     * 설정 값 가져오기
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * 설정 값 가져오기 (기본값 포함)
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 정수형 설정 값 가져오기
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 실수형 설정 값 가져오기
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Gemini API 키 가져오기
     */
    public String getGeminiApiKey() {
        String apiKey = getProperty("gemini.api.key");

        // Properties 파일에 ${GOOGLE_API_KEY} 형태로 작성된 경우 환경변수에서 가져오기
        if (apiKey != null && apiKey.startsWith("${") && apiKey.endsWith("}")) {
            String envVar = apiKey.substring(2, apiKey.length() - 1);
            apiKey = System.getenv(envVar);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException(
                "Gemini API key is not configured. " +
                "Please set 'gemini.api.key' in application.properties or " +
                "set GOOGLE_API_KEY environment variable."
            );
        }

        return apiKey;
    }

    /**
     * 벡터 검색 최대 결과 수
     */
    public int getVectorSearchMaxResults() {
        return getIntProperty("vector.search.max.results", 5);
    }

    /**
     * 벡터 검색 최소 점수
     */
    public double getVectorSearchMinScore() {
        return getDoubleProperty("vector.search.min.score", 0.5);
    }

    /**
     * RAG 청크 크기
     */
    public int getRagChunkSize() {
        return getIntProperty("rag.chunk.size", 500);
    }

    /**
     * RAG 청크 오버랩 크기
     */
    public int getRagChunkOverlap() {
        return getIntProperty("rag.chunk.overlap", 100);
    }

    /**
     * 임베딩 모델 이름
     */
    public String getEmbeddingModelName() {
        return getProperty("embedding.model.name", "text-embedding-004");
    }

    /**
     * 임베딩 차원
     */
    public int getEmbeddingDimension() {
        return getIntProperty("embedding.dimension", 768);
    }

    /**
     * Cohere API 키 가져오기
     */
    public String getCohereApiKey() {
        String apiKey = getProperty("cohere.api.key");

        // Properties 파일에 ${COHERE_API_KEY} 형태로 작성된 경우 환경변수에서 가져오기
        if (apiKey != null && apiKey.startsWith("${") && apiKey.endsWith("}")) {
            String envVar = apiKey.substring(2, apiKey.length() - 1);
            apiKey = System.getenv(envVar);
        }

        // Cohere API 키는 선택사항 (ReRanking이 비활성화될 수 있음)
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("Cohere API key is not configured. ReRanking will be disabled.");
            return null;
        }

        return apiKey;
    }

    /**
     * ReRanking 활성화 여부
     */
    public boolean isReRankingEnabled() {
        String enabled = getProperty("reranking.enabled", "false");
        return Boolean.parseBoolean(enabled);
    }

    /**
     * ReRanking 모델 이름
     */
    public String getReRankingModelName() {
        return getProperty("reranking.model.name", "rerank-multilingual-v3.0");
    }

    /**
     * ReRanking 초기 검색 결과 수
     */
    public int getReRankingInitialResults() {
        return getIntProperty("reranking.initial.results", 20);
    }

    /**
     * ReRanking 최종 결과 수
     */
    public int getReRankingFinalResults() {
        return getIntProperty("reranking.final.results", 5);
    }

    /**
     * ReRanking 최소 점수
     */
    public double getReRankingMinScore() {
        return getDoubleProperty("reranking.min.score", 0.8);
    }

    /**
     * Hybrid Search 활성화 여부
     */
    public boolean isHybridSearchEnabled() {
        String enabled = getProperty("hybrid.search.enabled", "false");
        return Boolean.parseBoolean(enabled);
    }

    /**
     * Hybrid Search Vector 가중치
     */
    public double getHybridVectorWeight() {
        return getDoubleProperty("hybrid.search.vector.weight", 0.6);
    }

    /**
     * Hybrid Search Keyword 가중치
     */
    public double getHybridKeywordWeight() {
        return getDoubleProperty("hybrid.search.keyword.weight", 0.4);
    }

    /**
     * Hybrid Search 초기 결과 수
     */
    public int getHybridInitialResults() {
        return getIntProperty("hybrid.search.initial.results", 40);
    }

    /**
     * BM25 인덱스 디렉토리
     */
    public String getBM25IndexDirectory() {
        String path = getProperty("bm25.index.directory", System.getProperty("user.home") + "/guideon/data/bm25-index");

        // ${user.home} 변수 해석
        if (path.contains("${user.home}")) {
            path = path.replace("${user.home}", System.getProperty("user.home"));
        }

        return path;
    }

    /**
     * BM25 K1 파라미터
     */
    public double getBM25K1() {
        return getDoubleProperty("bm25.k1", 1.2);
    }

    /**
     * BM25 B 파라미터
     */
    public double getBM25B() {
        return getDoubleProperty("bm25.b", 0.75);
    }
}
