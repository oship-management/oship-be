package org.example.oshipserver.domain.payment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import org.example.oshipserver.client.toss.TossPaymentClient;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.domain.payment.util.PaymentNoGenerator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
        // 1. Toss 결제 승인 API 호출
        TossPaymentConfirmResponse tossResponse = tossPaymentClient.requestPaymentConfirm(request);

        // 2. 오늘 날짜 기준 생성된 결제 수 조회하여 시퀀스 결정 (paymentNo 생성용)
        LocalDate today = LocalDate.now();
        int todayCount = paymentRepository.countByCreatedAtBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );

        // 3. 고유 paymentNo 생성
        String paymentNo = PaymentNoGenerator.generate(today, todayCount + 1);

        // 4. Toss 응답값을 Payment 엔티티로 변환하여 저장
        Payment payment = Payment.builder()
            .paymentNo(paymentNo)
            .orderId(request.orderId())
            .paymentKey(tossResponse.paymentKey())
            .amount(tossResponse.totalAmount())
            .currency("KRW")
            .method(PaymentMethod.CARD)
            .paidAt(LocalDateTime.parse(tossResponse.approvedAt()))
            .status(PaymentStatusMapper.fromToss(tossResponse.status()))
            .build();

        paymentRepository.save(payment);

        // 3. 응답 DTO 반환
        return PaymentConfirmResponse.convertFromTossConfirm(tossResponse);
    }

    private String getLast4Digits(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }
}
