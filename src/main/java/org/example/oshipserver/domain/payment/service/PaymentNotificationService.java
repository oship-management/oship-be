package org.example.oshipserver.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.entity.NotificationType;
import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
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
public class PaymentNotificationService {

    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;
    private final SellerRepository sellerRepository;

    /**
     * 결제 완료 알림
     */
    public void sendPaymentCompletedV2(Payment payment) {
        Seller seller = sellerRepository.findById(payment.getSellerId())
            .orElseThrow(() -> new ApiException("셀러 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        User user = userRepository.findById(seller.getUserId())
            .orElseThrow(() -> new ApiException("유저 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));


        NotificationRequest request = new NotificationRequest(
            NotificationType.PAYMENT_COMPLETED,
            "[OSH] 결제 완료 알림",
            "결제가 정상적으로 완료되었습니다.\n" +
                "결제번호: " + payment.getPaymentNo() + "\n" +
                "결제금액: " + payment.getAmount() + "원",
            user.getEmail()
        );

        emailNotificationService.send(request);
    }

    /**
     * 결제 취소 알림
     */
    public void sendPaymentCancelledV2(Payment payment) {
        Seller seller = sellerRepository.findById(payment.getSellerId())
            .orElseThrow(() -> new ApiException("셀러 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        User user = userRepository.findById(seller.getUserId())
            .orElseThrow(() -> new ApiException("유저 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        NotificationRequest request = new NotificationRequest(
            NotificationType.PAYMENT_CANCELLED,
            "[OSH] 결제 취소 알림",
            "결제가 취소되었습니다.\n" +
                "결제번호: " + payment.getPaymentKey() + "\n" +
                "환불금액: " + payment.getAmount() + "원",
            user.getEmail()
        );

        emailNotificationService.send(request);
    }

}
