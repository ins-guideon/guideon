package com.guideon.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequest {

    @NotBlank(message = "role 값은 필수입니다.")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

