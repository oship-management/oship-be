package org.example.oshipserver.domain.notification.service.sender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailSmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "oshipapp@gmail.com";

    @Override
    public void send(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true → HTML로 보냄
            helper.setFrom(FROM_EMAIL);

            mailSender.send(message);
            log.info("[Email 전송 성공] to={}, subject={}", to, subject);

        } catch (MessagingException e) {
            log.error("[Email 전송 실패] to={}, subject={}, reason={}", to, subject, e.getMessage());
            throw new RuntimeException("Gmail SMTP 메일 전송 실패", e);
        }
    }
}
