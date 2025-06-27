package org.example.oshipserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEmailSender {

    private final GmailSmtpEmailSender gmailSmtpEmailSender;

    @Async
    public void send(String to, String subject, String htmlContent) {
        gmailSmtpEmailSender.send(to, subject, htmlContent);
    }
}

