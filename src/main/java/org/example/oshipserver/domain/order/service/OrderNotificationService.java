package org.example.oshipserver.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.entity.NotificationType;
import org.example.oshipserver.domain.notification.repository.NotificationRepository;
import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.example.oshipserver.domain.notification.service.EmailTemplateService;
import org.example.oshipserver.domain.notification.service.async.AsyncEmailNotificationService;
import org.example.oshipserver.domain.notification.service.async.EmailNotificationProducer;
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

    public void sendOrderCreatedV2(Order order) {
        Seller seller = sellerRepository.findById(order.getSellerId())
            .orElseThrow(() -> new ApiException("셀러 없음", ErrorType.NOT_FOUND));

        User user = userRepository.findById(seller.getUserId())
            .orElseThrow(() -> new ApiException("유저 없음", ErrorType.NOT_FOUND));

        NotificationRequest request = new NotificationRequest(
            NotificationType.ORDER_CREATED,
            "[OSH] 주문 생성 알림",
            "주문번호 " + order.getOrderNo() + "가 생성되었습니다.",
            user.getEmail()
        );

        emailNotificationService.send(request);
    }
}