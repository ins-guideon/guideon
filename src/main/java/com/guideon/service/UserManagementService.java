package com.guideon.service;

import com.guideon.dto.UserDTO;
import com.guideon.model.Role;
import com.guideon.model.UserAccount;
import com.guideon.repository.UserAccountRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

    private final UserAccountRepository userRepository;

    public UserManagementService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUserRole(Long userId, String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("역할 정보가 필요합니다.");
        }

        Role role;
        try {
            role = Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("지원하지 않는 역할입니다: " + roleName);
        }

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setRole(role);
        return toDTO(user);
    }

    private UserDTO toDTO(UserAccount user) {
        return new UserDTO(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}

