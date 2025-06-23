package org.example.oshipserver.domain.notification.dto.message;

public record EmailNotificationMessage(
    String email,
    String subject,
    String content,
    int retryCount
) {}