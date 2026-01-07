package com.guideon.controller;

import com.guideon.config.GuideonProperties;
import com.guideon.dto.ApiResponse;
import com.guideon.dto.SettingsDTO;
import com.guideon.service.RegulationSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 설정 관리 REST API Controller
 * 전역적으로 설정을 관리합니다.
 */
@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "설정 API", description = "애플리케이션 설정 조회 및 업데이트 API")
public class SettingController {

    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

    private final GuideonProperties properties;
    private final RegulationSearchService regulationSearchService;
    private SettingsDTO currentSettings;

    public SettingController(GuideonProperties properties,
            RegulationSearchService regulationSearchService) {
        this.properties = properties;
        this.regulationSearchService = regulationSearchService;
        // 초기값은 GuideonProperties에서 가져오기
        this.currentSettings = initializeSettings();
        // RegulationSearchService에 초기 설정 적용
        regulationSearchService.applyConfig(currentSettings);
        logger.info("SettingController initialized with default settings from properties");
    }

    /**
     * GuideonProperties에서 초기 설정값 로드
     */
    private SettingsDTO initializeSettings() {
        SettingsDTO settings = new SettingsDTO();
        settings.setApiKey(properties.getGemini().getApi().getKey());
        settings.setSearchModel(properties.getGemini().getChat().getModelName());
        settings.setEmbeddingModel(properties.getEmbedding().getModel().getName());
        settings.setChunkSize(properties.getRag().getChunk().getSize());
        settings.setChunkOverlap(properties.getRag().getChunk().getOverlap());
        return settings;
    }

    /**
     * 현재 설정 조회
     */
    @Operation(summary = "설정 조회", description = "현재 애플리케이션 설정을 조회합니다.")
    @GetMapping
    public ApiResponse<SettingsDTO> getSettings() {
        try {
            logger.info("Settings retrieved successfully");
            return ApiResponse.success(currentSettings);
        } catch (Exception e) {
            logger.error("Error retrieving settings", e);
            return ApiResponse.error("설정 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 설정 업데이트
     */
    @Operation(summary = "설정 업데이트", description = "애플리케이션 설정을 업데이트합니다.")
    @PutMapping
    public ApiResponse<SettingsDTO> updateSettings(@Valid @RequestBody SettingsDTO newSettings) {
        if (newSettings == null) {
            return ApiResponse.error("설정은 null일 수 없습니다");
        }

        // 임시 설정 생성 (기존 설정과 병합)
        SettingsDTO updatedSettings = new SettingsDTO();
        updatedSettings
                .setApiKey(newSettings.getApiKey() != null ? newSettings.getApiKey() : currentSettings.getApiKey());
        updatedSettings.setSearchModel(
                newSettings.getSearchModel() != null ? newSettings.getSearchModel() : currentSettings.getSearchModel());
        updatedSettings.setEmbeddingModel(newSettings.getEmbeddingModel() != null ? newSettings.getEmbeddingModel()
                : currentSettings.getEmbeddingModel());
        updatedSettings.setChunkSize(
                newSettings.getChunkSize() != null ? newSettings.getChunkSize() : currentSettings.getChunkSize());
        updatedSettings.setChunkOverlap(newSettings.getChunkOverlap() != null ? newSettings.getChunkOverlap()
                : currentSettings.getChunkOverlap());

        try {
            logger.info("Applying settings: searchModel={}, embeddingModel={}, chunkSize={}, chunkOverlap={}",
                    updatedSettings.getSearchModel(), updatedSettings.getEmbeddingModel(),
                    updatedSettings.getChunkSize(), updatedSettings.getChunkOverlap());

            // RegulationSearchService에 설정 적용
            regulationSearchService.applyConfig(updatedSettings);

            // 성공 시에만 currentSettings 업데이트
            currentSettings = updatedSettings;

            logger.info("Settings updated successfully");
            return ApiResponse.success(currentSettings);

        } catch (Exception e) {
            logger.error("Error updating settings", e);
            return ApiResponse.error("설정 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
