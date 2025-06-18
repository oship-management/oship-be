package org.example.oshipserver.domain.notification.service;

public interface EmailSender {
    void send(String to, String subject, String content);
}