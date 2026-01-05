package com.guideon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Home Controller - 프론트엔드 정적 파일 서빙 및 API 정보
 */
@Controller
public class HomeController {

    /**
     * 프론트엔드 엔트리 포인트 (index.html)
     * API, H2 콘솔, Swagger 등을 제외한 모든 경로를 index.html로 포워딩 (SPA 지원)
     */
    @GetMapping(value = {"/", "/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String index() {
        return "forward:/index.html";
    }

    /**
     * API 정보 반환 (기존 기능 유지 - 경로 변경)
     */
    @GetMapping("/api/info")
    @ResponseBody
    public Map<String, Object> apiInfo() {
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
     * 헬스 체크
     */
    @GetMapping("/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Guideon REST API");
        return status;
    }
}
