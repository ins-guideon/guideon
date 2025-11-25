package com.guideon.controller;

import com.guideon.dto.ApiResponse;
import com.guideon.dto.UpdateUserRoleRequest;
import com.guideon.dto.UserDTO;
import com.guideon.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "관리자 사용자 API", description = "관리자가 사용자 권한을 관리하는 API")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @Operation(summary = "사용자 목록 조회", description = "등록된 모든 사용자와 역할 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<List<UserDTO>> getUsers() {
        logger.info("관리자 사용자 목록 조회");
        try {
            return ApiResponse.success(userManagementService.getAllUsers());
        } catch (Exception e) {
            logger.error("사용자 목록 조회 중 오류", e);
            return ApiResponse.error("사용자 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "사용자 역할 변경", description = "특정 사용자의 역할을 변경합니다.")
    @PutMapping("/{userId}/role")
    public ApiResponse<UserDTO> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        logger.info("관리자 사용자 역할 변경 요청: userId={}", userId);
        try {
            UserDTO updated = userManagementService.updateUserRole(userId, request.getRole());
            return ApiResponse.success(updated);
        } catch (Exception e) {
            logger.warn("사용자 역할 변경 실패: userId={}, error={}", userId, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}

