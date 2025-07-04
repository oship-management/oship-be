package org.example.oshipserver.domain.notification.service.sender;

public interface EmailSender {
    void send(String to, String subject, String content);
}