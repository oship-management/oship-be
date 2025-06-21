package org.example.oshipserver.domain.notification.dto.request;

import org.example.oshipserver.domain.notification.entity.NotificationType;

public record NotificationRequest(
    NotificationType type,
    String title,
    String content,
    String targetEmail
) {}
