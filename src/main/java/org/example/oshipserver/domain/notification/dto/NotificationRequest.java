package org.example.oshipserver.domain.notification.dto;

public record NotificationRequest(
    String type,
    String title,
    String content,
    Long sellerId
) {}