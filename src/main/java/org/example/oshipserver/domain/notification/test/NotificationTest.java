package org.example.oshipserver.domain.notification.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.NotificationRequest;
import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("local") // 배포 환경에서는 실행되지 않도록 제한
public class NotificationTest implements CommandLineRunner {

    private final EmailNotificationService emailNotificationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("이메일 발송 테스트 시작...");

        Long testSellerId = 1L; // 실제 이메일 연결된 sellerId
        NotificationRequest request = new NotificationRequest(
            "ORDER_PLACED",   // type
            "[테스트] 주문 알림",   // title
            "이것은 테스트 알림입니다.\n마스터 주문번호: TEST-123456", // content
            testSellerId
        );

        // 알림 서비스 실행 (DB 저장 + 이메일 발송)
        emailNotificationService.send(request);

        log.info("테스트 이메일 발송 완료");
    }
}