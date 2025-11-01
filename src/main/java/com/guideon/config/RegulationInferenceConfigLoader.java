package com.guideon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 규정 유형 추론 설정 로더
 * regulation-inference-rules.yaml 파일을 읽어서 RegulationInferenceConfig 객체로 변환
 */
public class RegulationInferenceConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(RegulationInferenceConfigLoader.class);
    private static final String CONFIG_FILE = "regulation-inference-rules.yaml";

    private static RegulationInferenceConfig cachedConfig = null;

    /**
     * 설정 파일 로드 (싱글톤 패턴)
     */
    public static RegulationInferenceConfig load() {
        if (cachedConfig != null) {
            return cachedConfig;
        }

        logger.info("Loading regulation inference rules from: {}", CONFIG_FILE);

        try (InputStream inputStream = RegulationInferenceConfigLoader.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (inputStream == null) {
                logger.error("Configuration file not found: {}", CONFIG_FILE);
                return createDefaultConfig();
            }

            Yaml yaml = new Yaml();
            cachedConfig = yaml.loadAs(inputStream, RegulationInferenceConfig.class);

            // 유효성 검증
            validateConfig(cachedConfig);

            logger.info("✓ Regulation inference rules loaded successfully");
            logger.info("  - Version: {}", cachedConfig.getVersion());
            logger.info("  - Total rules: {}", cachedConfig.getRules().size());
            logger.info("  - Enabled rules: {}",
                    cachedConfig.getRules().stream().filter(r -> r.getEnabled()).count());
            logger.info("  - Settings: {}", cachedConfig.getSettings());

            return cachedConfig;

        } catch (Exception e) {
            logger.error("Failed to load regulation inference rules", e);
            return createDefaultConfig();
        }
    }

    /**
     * 설정 유효성 검증
     */
    private static void validateConfig(RegulationInferenceConfig config) {
        if (config.getRules() == null || config.getRules().isEmpty()) {
            logger.warn("No inference rules defined in configuration");
        }

        if (config.getSettings() == null) {
            logger.warn("No settings defined, using defaults");
            config.setSettings(new RegulationInferenceConfig.Settings());
        }

        // 각 규칙 검증
        int invalidRules = 0;
        for (RegulationInferenceConfig.InferenceRule rule : config.getRules()) {
            if (rule.getKeywords() == null || rule.getKeywords().isEmpty()) {
                logger.warn("Rule '{}' has no keywords, will be ignored", rule.getName());
                invalidRules++;
            }
            if (rule.getRegulationTypes() == null || rule.getRegulationTypes().isEmpty()) {
                logger.warn("Rule '{}' has no regulation types, will be ignored", rule.getName());
                invalidRules++;
            }
        }

        if (invalidRules > 0) {
            logger.warn("Found {} invalid rules in configuration", invalidRules);
        }
    }

    /**
     * 기본 설정 생성 (폴백용)
     */
    private static RegulationInferenceConfig createDefaultConfig() {
        logger.info("Creating default regulation inference config");

        RegulationInferenceConfig config = new RegulationInferenceConfig();
        config.setVersion("1.0-default");
        config.setRules(new ArrayList<>());

        // 기본 규칙 하나만 추가 (복리후생)
        RegulationInferenceConfig.InferenceRule defaultRule = new RegulationInferenceConfig.InferenceRule();
        defaultRule.setName("기본 규칙");
        defaultRule.setDescription("기본 복리후생 규칙");
        defaultRule.setKeywords(List.of("경조사", "휴가", "복리후생"));
        defaultRule.setRegulationTypes(List.of("복리후생비규정", "취업규칙"));
        defaultRule.setPriority(100);
        defaultRule.setEnabled(true);

        config.getRules().add(defaultRule);

        // 기본 설정
        RegulationInferenceConfig.Settings settings = new RegulationInferenceConfig.Settings();
        settings.setDefaultRegulationType("일반");
        settings.setMaxRegulationTypes(5);
        settings.setMinPriority(50);
        settings.setMatchMode("contains");
        config.setSettings(settings);

        return config;
    }

    /**
     * 캐시 초기화 (테스트/리로드용)
     */
    public static void clearCache() {
        cachedConfig = null;
        logger.info("Regulation inference config cache cleared");
    }

    /**
     * 특정 쿼리에 매칭되는 규정 유형 추론
     *
     * @param query 사용자 쿼리
     * @return 추론된 규정 유형 목록
     */
    public static List<String> inferRegulationTypes(String query) {
        RegulationInferenceConfig config = load();
        List<String> inferredTypes = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return List.of(config.getSettings().getDefaultRegulationType());
        }

        String lowerQuery = query.toLowerCase();

        // 모든 규칙을 우선순위 순으로 정렬
        List<RegulationInferenceConfig.InferenceRule> sortedRules = config.getRules().stream()
                .filter(rule -> rule.getEnabled())
                .filter(rule -> rule.getPriority() >= config.getSettings().getMinPriority())
                .sorted((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()))
                .collect(Collectors.toList());

        // 각 규칙을 순회하며 매칭 확인
        for (RegulationInferenceConfig.InferenceRule rule : sortedRules) {
            if (matchesRule(lowerQuery, rule, config.getSettings().getMatchMode())) {
                // 매칭된 규정 유형 추가 (중복 제거)
                for (String regType : rule.getRegulationTypes()) {
                    if (!inferredTypes.contains(regType)) {
                        inferredTypes.add(regType);
                    }
                }

                logger.debug("Rule '{}' matched for query: {}", rule.getName(), query);

                // 최대 개수 제한
                if (inferredTypes.size() >= config.getSettings().getMaxRegulationTypes()) {
                    break;
                }
            }
        }

        // 매칭된 규칙이 없으면 기본값 반환
        if (inferredTypes.isEmpty()) {
            inferredTypes.add(config.getSettings().getDefaultRegulationType());
            logger.debug("No rules matched, using default: {}",
                    config.getSettings().getDefaultRegulationType());
        } else {
            logger.info("Inferred regulation types for '{}': {}", query, inferredTypes);
        }

        return inferredTypes;
    }

    /**
     * 쿼리가 규칙과 매칭되는지 확인
     */
    private static boolean matchesRule(String lowerQuery,
                                       RegulationInferenceConfig.InferenceRule rule,
                                       String matchMode) {
        for (String keyword : rule.getKeywords()) {
            String lowerKeyword = keyword.toLowerCase();

            boolean matches = switch (matchMode) {
                case "exact" -> lowerQuery.equals(lowerKeyword);
                case "contains" -> lowerQuery.contains(lowerKeyword);
                case "regex" -> lowerQuery.matches(lowerKeyword);
                default -> lowerQuery.contains(lowerKeyword);
            };

            if (matches) {
                return true;
            }
        }
        return false;
    }

    /**
     * 설정 정보 출력 (디버깅용)
     */
    public static void printConfig() {
        RegulationInferenceConfig config = load();

        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  Regulation Inference Rules Configuration            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Version: " + config.getVersion());
        System.out.println("Total Rules: " + config.getRules().size());
        System.out.println("Settings: " + config.getSettings());
        System.out.println();
        System.out.println("Rules:");
        System.out.println("─────────────────────────────────────────────────────────");

        for (RegulationInferenceConfig.InferenceRule rule : config.getRules()) {
            System.out.println();
            System.out.printf("• %s%n", rule.getName());
            System.out.printf("  Description: %s%n", rule.getDescription());
            System.out.printf("  Keywords (%d): %s%n",
                    rule.getKeywords().size(),
                    rule.getKeywords().size() > 5
                            ? rule.getKeywords().subList(0, 5) + "..."
                            : rule.getKeywords());
            System.out.printf("  Regulation Types: %s%n", rule.getRegulationTypes());
            System.out.printf("  Priority: %d | Enabled: %s%n",
                    rule.getPriority(), rule.getEnabled() ? "✓" : "✗");
        }

        System.out.println();
        System.out.println("═════════════════════════════════════════════════════════");
    }
}
