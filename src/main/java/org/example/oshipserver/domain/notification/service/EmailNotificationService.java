package org.example.oshipserver.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.notification.dto.NotificationRequest;
import org.example.oshipserver.domain.notification.entity.Notification;
import org.example.oshipserver.domain.notification.repository.NotificationRepository;
import org.example.oshipserver.domain.seller.entity.Seller;
import org.example.oshipserver.domain.seller.repository.SellerRepository;
import org.example.oshipserver.domain.user.entity.User;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailNotificationService {

    private final EmailSender emailSender;
    private final NotificationRepository notificationRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public void send(NotificationRequest request) {
        Notification notification = Notification.builder()
            .type(request.type())
            .title(request.title())
            .content(request.content())
            .sent(false)
            .build();

        try {
            Seller seller = sellerRepository.findById(request.sellerId())
                .orElseThrow(() -> new ApiException("셀러를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

            User user = userRepository.findById(seller.getUserId())
                .orElseThrow(() -> new ApiException("유저를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

            String to = user.getEmail();

            emailSender.send(
                to,
                request.title(),
                request.content()
            );

            notification.markAsSent();

            // setter 추후 수정 예정
            notification.setTargetEmail(to);

        } catch (Exception e) {
            log.error("[알림 전송 실패] type={}, sellerId={}, reason={}",
                request.type(), request.sellerId(), e.getMessage());
        }

        notificationRepository.save(notification);
    }
}
