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
            logger.info("API key overridden by environment variable");
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
        return getProperty("embedding.model.name", "all-minilm-l6-v2");
    }
}
