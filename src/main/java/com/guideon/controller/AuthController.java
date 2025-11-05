package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.LoginRequest;
import com.guideon.dto.LoginResponse;
import com.guideon.dto.RegisterRequest;
import com.guideon.dto.UserDTO;
import com.guideon.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("로그인 시도: username={}", request.getUsername());
        try {
            LoginResponse res = authService.login(request);
            logger.info("로그인 성공: username={}", request.getUsername());
            return ApiResponse.success(res);
        } catch (Exception e) {
            logger.warn("로그인 실패: username={}, error={}", request.getUsername(), e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("회원가입 요청: username={}", request.getUsername());
        try {
            UserDTO user = authService.register(request);
            return ApiResponse.success(user);
        } catch (Exception e) {
            logger.warn("회원가입 실패: username={}, error={}", request.getUsername(), e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser(Authentication authentication) {
        logger.info("현재 사용자 정보 조회 요청");
        return ApiResponse.success(authService.getCurrentUser(authentication.getName()));
    }
}
