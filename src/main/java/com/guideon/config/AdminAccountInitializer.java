package com.guideon.config;

import com.guideon.model.Role;
import com.guideon.model.UserAccount;
import com.guideon.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "passwd";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@guideon.local";

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
            logger.info("기본 관리자 계정이 이미 존재합니다. username={}", DEFAULT_ADMIN_USERNAME);
            return;
        }

        String email = DEFAULT_ADMIN_EMAIL;
        if (userRepository.existsByEmail(email)) {
            email = System.currentTimeMillis() + "+" + DEFAULT_ADMIN_EMAIL;
            logger.warn("기본 관리자 이메일이 이미 사용 중입니다. 대체 이메일을 사용합니다. email={}", email);
        }

        UserAccount admin = new UserAccount(
                DEFAULT_ADMIN_USERNAME,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                "시스템 관리자",
                email,
                Role.ADMIN
        );

        userRepository.save(admin);
        logger.info("기본 관리자 계정을 생성했습니다. username={}", DEFAULT_ADMIN_USERNAME);
    }
}

