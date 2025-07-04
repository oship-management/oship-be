package org.example.oshipserver.domain.payment.service;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.seller.entity.Seller;
import org.example.oshipserver.domain.seller.repository.SellerRepository;
import org.example.oshipserver.domain.user.entity.User;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PaymentNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private PaymentNotificationService paymentNotificationService;

    private Payment payment;
    private Seller seller;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 기본 mock 객체 생성
        seller = Seller.builder()
            .id(1L)
            .userId(2L)
            .build();

        user = User.builder()
            .id(2L)
            .email("test@example.com")
            .build();

        payment = Payment.builder()
            .paymentKey("test-payment-key")
            .amount(10000)
            .sellerId(1L)
            .build();
    }

    @Test
    @DisplayName("결제 완료 알림 전송 성공")
    void sendPaymentCompletedV2_success() {
        when(sellerRepository.findByUserId(1L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        paymentNotificationService.sendPaymentCompletedV2(payment);

        verify(emailNotificationService, times(1)).send(any());
    }

    @Test
    @DisplayName("결제 취소 알림 전송 성공")
    void sendPaymentCancelledV2_success() {
        when(sellerRepository.findByUserId(1L)).thenReturn(Optional.of(seller));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        paymentNotificationService.sendPaymentCancelledV2(payment);

        verify(emailNotificationService, times(1)).send(any());
    }
}
