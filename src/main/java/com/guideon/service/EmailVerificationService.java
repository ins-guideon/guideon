package com.guideon.service;

import com.guideon.model.EmailVerification;
import com.guideon.repository.EmailVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(EmailVerificationRepository verificationRepository, EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendCode(String email) {
        // 기존 인증 정보 삭제 (새로운 인증을 위해)
        verificationRepository.deleteByEmail(email);

        // 6자리 인증번호 생성
        String code = String.format("%06d", random.nextInt(1000000));
        
        // 인증 정보 저장 (10분 유효)
        EmailVerification verification = new EmailVerification(email, code, 10);
        verificationRepository.save(verification);

        // 이메일 발송
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        Optional<EmailVerification> verificationOpt = verificationRepository.findByEmailAndCode(email, code);
        
        if (verificationOpt.isPresent()) {
            EmailVerification verification = verificationOpt.get();
            if (!verification.isExpired()) {
                verification.setVerified(true);
                verificationRepository.save(verification);
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return verificationRepository.findTopByEmailOrderByExpiryDateDesc(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }
}

