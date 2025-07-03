package org.example.oshipserver.domain.notification.service.async;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEmailNotificationService {

    private final EmailNotificationService emailNotificationService;

    @Async
    public void sendAsync(NotificationRequest request) {
        emailNotificationService.send(request);
    }
}

