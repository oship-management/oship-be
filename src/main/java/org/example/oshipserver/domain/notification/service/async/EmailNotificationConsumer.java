package org.example.oshipserver.domain.notification.service.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.message.EmailNotificationMessage;
import org.example.oshipserver.domain.notification.repository.NotificationRepository;
import org.example.oshipserver.domain.notification.service.sender.EmailSender;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;
    private final NotificationRepository notificationRepository;

    private static final String QUEUE_NAME = "email-notification-queue";
    private static final String DLQ_NAME = "email-notification-dlq";

    @Scheduled(fixedDelay = 2000)
    public void pollAndSendEmail() {
        String json = redisTemplate.opsForList().rightPop(QUEUE_NAME);
        if (json == null) return;

        try {
            EmailNotificationMessage message = objectMapper.readValue(json, EmailNotificationMessage.class);

            emailSender.send(message.email(), message.subject(), message.content());
            log.info("[이메일 전송 성공] to={}, subject={}", message.email(), message.subject());

            // 전송 성공 시 Notification 업데이트
            if (message.notificationId() != null) {
                notificationRepository.findById(message.notificationId())
                    .ifPresent(notification -> {
                        notification.markAsSent();
                        notificationRepository.save(notification);
                    });
            }

        } catch (Exception e) {
            handleRetry(json);
        }
    }

    private void handleRetry(String json) {
        try {
            EmailNotificationMessage msg = objectMapper.readValue(json, EmailNotificationMessage.class);
            int retry = msg.retryCount() + 1;

            if (retry < 3) {
                EmailNotificationMessage retryMsg = new EmailNotificationMessage(
                    msg.email(), msg.subject(), msg.content(), retry, msg.notificationId()
                );
                redisTemplate.opsForList().leftPush(QUEUE_NAME, objectMapper.writeValueAsString(retryMsg));
                log.warn("[이메일 재시도] {}회 → to={}", retry, msg.email());
            } else {
                redisTemplate.opsForList().leftPush(DLQ_NAME, json);
                log.error("[이메일 전송 실패 - DLQ 이동] to={}, subject={}", msg.email(), msg.subject());
            }
        } catch (Exception ex) {
            log.error("[DLQ 처리 중 에러] {}", ex.getMessage());
        }
    }
}
