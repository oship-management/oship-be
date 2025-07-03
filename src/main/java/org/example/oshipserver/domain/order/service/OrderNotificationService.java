package org.example.oshipserver.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.message.EmailNotificationMessage;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.entity.Notification;
import org.example.oshipserver.domain.notification.entity.NotificationType;
import org.example.oshipserver.domain.notification.repository.NotificationRepository;
import org.example.oshipserver.domain.notification.service.async.AsyncEmailNotificationService;
import org.example.oshipserver.domain.notification.service.async.EmailNotificationProducer;
import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.example.oshipserver.domain.notification.service.EmailTemplateService;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.seller.entity.Seller;
import org.example.oshipserver.domain.seller.repository.SellerRepository;
import org.example.oshipserver.domain.user.entity.User;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;         // 동기
    private final AsyncEmailNotificationService asyncEmailNotificationService; // 비동기
    private final EmailNotificationProducer emailNotificationProducer;
    private final EmailTemplateService emailTemplateService;
    private final NotificationRepository notificationRepository;

    public void sendOrderCreatedSync(Order order) {
        NotificationRequest request = buildNotificationRequest(order);
        emailNotificationService.send(request);
    }

    public void sendOrderCreatedAsync(Order order) {
        NotificationRequest request = buildNotificationRequest(order);
        asyncEmailNotificationService.sendAsync(request);
    }

    public void sendOrderCreatedV2(Order order) {
        Seller seller = sellerRepository.findById(order.getSellerId())
            .orElseThrow(() -> new ApiException("셀러 없음", ErrorType.NOT_FOUND));

        User user = userRepository.findById(seller.getUserId())
            .orElseThrow(() -> new ApiException("유저 없음", ErrorType.NOT_FOUND));

        String subject = "[OSH] 주문 생성 알림";
        String content = "주문번호 " + order.getOrderNo() + "가 생성되었습니다.";

        // 1. Notification 엔티티 생성 및 저장
        Notification notification = Notification.builder()
            .type(NotificationType.ORDER_CREATED)
            .title(subject)
            .content(content)
            .targetEmail(user.getEmail())
            .sent(false)
            .build();
        notificationRepository.save(notification);

        // 2. 이메일 HTML 본문 생성
        try {
            String html = emailTemplateService.renderEmail(subject, content);

            // 3. Redis 큐 전송
            EmailNotificationMessage message = new EmailNotificationMessage(
                user.getEmail(),
                subject,
                html,
                0,
                notification.getId() // Notification과 연동된 메시지
            );
            emailNotificationProducer.send(message);
        } catch (Exception e) {
            // 템플릿 렌더링 실패 시 로그만 남기고 전송 생략
            log.error("[알림 템플릿 렌더링 실패] orderNo={}, email={}, reason={}",
                order.getOrderNo(), user.getEmail(), e.getMessage());
        }
    }

    private NotificationRequest buildNotificationRequest(Order order) {
        Seller seller = sellerRepository.findById(order.getSellerId())
            .orElseThrow(() -> new ApiException("셀러 없음", ErrorType.NOT_FOUND));

        User user = userRepository.findById(seller.getUserId())
            .orElseThrow(() -> new ApiException("유저 없음", ErrorType.NOT_FOUND));

        return new NotificationRequest(
            NotificationType.ORDER_CREATED,
            "[OSH] 주문 생성 알림",
            "주문번호 " + order.getOrderNo() + "가 생성되었습니다.",
            user.getEmail()
        );
    }
}
