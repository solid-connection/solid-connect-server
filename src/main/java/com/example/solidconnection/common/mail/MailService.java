package com.example.solidconnection.common.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[Solid Connect] 학교 이메일 인증");
        message.setText("인증 코드: " + verificationCode + "\n\n인증 코드는 5분간 유효합니다.");
        javaMailSender.send(message);
    }
}
