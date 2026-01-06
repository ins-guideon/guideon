package com.guideon.repository;

import com.guideon.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByExpiryDateDesc(String email);
    Optional<EmailVerification> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
}

