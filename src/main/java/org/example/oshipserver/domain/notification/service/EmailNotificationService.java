package org.example.oshipserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.entity.Notification;
import org.example.oshipserver.domain.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailNotificationService {

    private final EmailSender emailSender;
    private final NotificationRepository notificationRepository;
    private final EmailTemplateService emailTemplateService;

    public void send(NotificationRequest request) {
        Notification notification = Notification.builder()
            .type(request.type())
            .title(request.title())
            .content(request.content())
            .targetEmail(request.targetEmail())
            .sent(false)
            .build();

        try {
            // content를 템플릿으로 감싸기
            String html = emailTemplateService.renderEmail(
                request.title(),
                request.content()
            );

            emailSender.send(
                request.targetEmail(),
                request.title(),
                html
            );
            notification.markAsSent();
        } catch (Exception e) {
            log.error("[알림 전송 실패] type={}, email={}, reason={}",
                request.type(), request.targetEmail(), e.getMessage());
        }

        notificationRepository.save(notification);
    }
}

