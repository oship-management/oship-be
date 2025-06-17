package org.example.oshipserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailSmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    private static final String FROM_EMAIL = "oshipapp@gmail.com";

    @Override
    public void send(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom(FROM_EMAIL);

            mailSender.send(message);

            log.info("[Email 전송 성공] to={}, subject={}", to, subject);

        } catch (Exception e) {
            log.error("[Email 전송 실패] to={}, subject={}, reason={}", to, subject, e.getMessage());
            throw new RuntimeException("Gmail SMTP 메일 전송 실패", e);
        }
    }
}