package com.guideon.service;

import com.guideon.dto.LoginRequest;
import com.guideon.dto.LoginResponse;
import com.guideon.dto.RegisterRequest;
import com.guideon.dto.UserDTO;
import com.guideon.model.Role;
import com.guideon.model.UserAccount;
import com.guideon.repository.UserAccountRepository;
import com.guideon.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        UserAccount user = new UserAccount(
                request.getUsername(),
                passwordHash,
                request.getName(),
                request.getEmail(),
                Role.USER
        );
        UserAccount saved = userRepository.save(user);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        boolean remember = request.getRememberMe() != null && request.getRememberMe();
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name(), remember);
        return new LoginResponse(token, toDTO(user));
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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


