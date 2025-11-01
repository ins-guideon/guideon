package com.guideon.config;

import java.util.List;

/**
 * 규정 유형 추론 설정
 * regulation-inference-rules.yaml 파일의 구조를 매핑
 */
public class RegulationInferenceConfig {

    private String version;
    private List<InferenceRule> rules;
    private Settings settings;

    // Getters and Setters
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<InferenceRule> getRules() {
        return rules;
    }

    public void setRules(List<InferenceRule> rules) {
        this.rules = rules;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * 개별 추론 규칙
     */
    public static class InferenceRule {
        private String name;
        private String description;
        private List<String> keywords;
        private List<String> regulationTypes;
        private Integer priority;
        private Boolean enabled;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }

        public List<String> getRegulationTypes() {
            return regulationTypes;
        }

        public void setRegulationTypes(List<String> regulationTypes) {
            this.regulationTypes = regulationTypes;
        }

        public Integer getPriority() {
            return priority != null ? priority : 100;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public Boolean getEnabled() {
            return enabled != null ? enabled : true;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "InferenceRule{" +
                    "name='" + name + '\'' +
                    ", keywords=" + keywords.size() +
                    ", regulationTypes=" + regulationTypes +
                    ", priority=" + getPriority() +
                    ", enabled=" + getEnabled() +
                    '}';
        }
    }

    /**
     * 전역 설정
     */
    public static class Settings {
        private String defaultRegulationType;
        private Integer maxRegulationTypes;
        private Integer minPriority;
        private String matchMode;
        private String logLevel;

        // Getters and Setters
        public String getDefaultRegulationType() {
            return defaultRegulationType != null ? defaultRegulationType : "일반";
        }

        public void setDefaultRegulationType(String defaultRegulationType) {
            this.defaultRegulationType = defaultRegulationType;
        }

        public Integer getMaxRegulationTypes() {
            return maxRegulationTypes != null ? maxRegulationTypes : 5;
        }

        public void setMaxRegulationTypes(Integer maxRegulationTypes) {
            this.maxRegulationTypes = maxRegulationTypes;
        }

        public Integer getMinPriority() {
            return minPriority != null ? minPriority : 50;
        }

        public void setMinPriority(Integer minPriority) {
            this.minPriority = minPriority;
        }

        public String getMatchMode() {
            return matchMode != null ? matchMode : "contains";
        }

        public void setMatchMode(String matchMode) {
            this.matchMode = matchMode;
        }

        public String getLogLevel() {
            return logLevel != null ? logLevel : "INFO";
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        @Override
        public String toString() {
            return "Settings{" +
                    "defaultRegulationType='" + getDefaultRegulationType() + '\'' +
                    ", maxRegulationTypes=" + getMaxRegulationTypes() +
                    ", minPriority=" + getMinPriority() +
                    ", matchMode='" + getMatchMode() + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "RegulationInferenceConfig{" +
                "version='" + version + '\'' +
                ", rules=" + (rules != null ? rules.size() : 0) +
                ", settings=" + settings +
                '}';
    }
}
