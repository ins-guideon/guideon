package com.guideon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[Guideon] 회원가입 이메일 인증번호");
            message.setText("안녕하세요,\n\nGuideon 회원가입을 위한 인증번호는 다음과 같습니다:\n\n" + 
                           code + "\n\n인증번호는 10분 동안 유효합니다.\n\n감사합니다.");
            mailSender.send(message);
            logger.info("이메일 전송 완료: {}", to);
        } catch (Exception e) {
            logger.error("이메일 전송 실패: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다. 관리자에게 문의하세요.");
        }
    }
}

