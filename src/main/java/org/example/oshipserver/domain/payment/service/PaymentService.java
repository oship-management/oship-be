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
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.example.oshipserver.global.exception.ApiException;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
        // 1. Toss 결제 승인 API 호출
        TossPaymentConfirmResponse tossResponse = tossPaymentClient.requestPaymentConfirm(request);

        // 2. 중복 결제 여부 확인
        if (paymentRepository.existsByPaymentKey(tossResponse.paymentKey())) {
            throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
        }

        // 3. 오늘 날짜 기준 생성된 결제 수 조회하여 시퀀스 결정 (paymentNo 생성용)
        LocalDate today = LocalDate.now();
        int todayCount = paymentRepository.countByCreatedAtBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );

        // 4. 고유 paymentNo 생성
        String paymentNo = PaymentNoGenerator.generate(today, todayCount + 1);

        // 5. Toss 응답값을 Payment 엔티티로 변환하여 저장
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

        // 6. 응답 DTO 반환
        return PaymentConfirmResponse.convertFromTossConfirm(tossResponse);
    }

}
