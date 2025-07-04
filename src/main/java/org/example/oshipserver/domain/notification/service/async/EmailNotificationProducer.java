package org.example.oshipserver.domain.notification.service.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.message.EmailNotificationMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationProducer {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "email-notification-queue";

    public EmailNotificationProducer(
        @Qualifier("stringQueueRedisTemplate") RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(EmailNotificationMessage message) {
        try {
            log.info("직렬화 전 EmailNotificationMessage: {}", message);

            String json = objectMapper.writeValueAsString(message);
            log.info("직렬화 후 JSON 문자열: {}", json);

            Long listSize = redisTemplate.opsForList().leftPush(QUEUE_NAME, json);
            log.info("Redis leftPush 완료, 현재 큐 사이즈: {}", listSize);

            log.info("[Redis 큐 적재 최종 성공] key={}, value={}", QUEUE_NAME, json);
        } catch (JsonProcessingException e) {
            log.error("[Redis 큐 직렬화 실패] {}", e.getMessage());
            throw new RuntimeException("Email 메시지 직렬화 실패", e);
        }
    }

}

