package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.LoginRequest;
import com.guideon.dto.LoginResponse;
import com.guideon.dto.RegisterRequest;
import com.guideon.dto.UserDTO;
import com.guideon.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증 API", description = "사용자 인증 및 회원가입 API")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 발급받습니다.")
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

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
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

    @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser(Authentication authentication) {
        logger.info("현재 사용자 정보 조회 요청");
        return ApiResponse.success(authService.getCurrentUser(authentication.getName()));
    }
}
