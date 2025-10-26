package com.guideon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Home Controller - API 정보 및 헬스 체크
 */
@RestController
public class HomeController {

    /**
     * 루트 경로 - API 정보 반환
     */
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Guideon - AI-powered Regulation Search System");
        info.put("version", "1.0.0");
        info.put("status", "running");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "GET /actuator/health");
        endpoints.put("analyze", "POST /api/qa/analyze");
        endpoints.put("search", "POST /api/qa/search");
        endpoints.put("regulationTypes", "GET /api/regulations/types");
        endpoints.put("upload", "POST /api/regulations/upload");

        info.put("endpoints", endpoints);
        info.put("documentation", "https://github.com/guideon");

        return info;
    }

    /**
     * 헬스 체크 (간단한 버전)
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Guideon REST API");
        return status;
    }
}
