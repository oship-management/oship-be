package org.example.oshipserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.message.EmailNotificationMessage;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.entity.Notification;
import org.example.oshipserver.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailNotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailTemplateService emailTemplateService;
    private final EmailNotificationProducer emailNotificationProducer;

    public void send(NotificationRequest request) {

        // 1. Notification 저장 (ID 확보)
        Notification notification = Notification.builder()
            .type(request.type())
            .title(request.title())
            .content(request.content())
            .targetEmail(request.targetEmail())
            .sent(false)
            .build();
        notificationRepository.save(notification);

        try {
            // 2. 템플릿 변환 + 메시지 생성
            String html = emailTemplateService.renderEmail(
                request.title(),
                request.content()
            );

            EmailNotificationMessage message = new EmailNotificationMessage(
                request.targetEmail(),
                request.title(),
                html,
                0,
                notification.getId()
            );

            // 3. Redis 큐에 push
            emailNotificationProducer.send(message);

        } catch (Exception e) {
            log.error("[Producer 전송 실패] email={}, reason={}", request.targetEmail(), e.getMessage());
        }
    }
}
