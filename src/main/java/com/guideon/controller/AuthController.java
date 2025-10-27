package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.LoginRequest;
import com.guideon.dto.LoginResponse;
import com.guideon.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        logger.info("로그인 시도: username={}", request.getUsername());

        try {
            // TODO: 실제 인증 로직 구현 필요
            // 현재는 테스트용으로 admin/admin 만 허용
            if ("admin".equals(request.getUsername()) && "admin".equals(request.getPassword())) {
                // 임시 토큰 생성
                String token = "Bearer " + UUID.randomUUID().toString();

                // 사용자 정보 생성
                UserDTO user = new UserDTO(
                    "1",
                    request.getUsername(),
                    "관리자",
                    "admin@guideon.com",
                    "ADMIN"
                );

                LoginResponse loginResponse = new LoginResponse(token, user);
                logger.info("로그인 성공: username={}", request.getUsername());

                return ApiResponse.success(loginResponse);
            } else {
                logger.warn("로그인 실패: 잘못된 인증 정보 - username={}", request.getUsername());
                return ApiResponse.error("아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (Exception e) {
            logger.error("로그인 처리 중 오류 발생", e);
            return ApiResponse.error("로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        logger.info("로그아웃 요청");
        // TODO: 실제 로그아웃 로직 구현 (세션 무효화, 토큰 삭제 등)
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser() {
        logger.info("현재 사용자 정보 조회 요청");

        // TODO: 실제 인증된 사용자 정보 반환
        // 현재는 테스트용 데이터 반환
        UserDTO user = new UserDTO(
            "1",
            "admin",
            "관리자",
            "admin@guideon.com",
            "ADMIN"
        );

        return ApiResponse.success(user);
    }
}
