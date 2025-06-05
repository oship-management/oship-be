package org.example.oshipserver.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import org.example.oshipserver.client.toss.TossPaymentClient;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossSinglePaymentLookupResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentMethodMapper;
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

    // 단건 결제 생성 API
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {

        // 1. 중복 결제 여부 확인
        if (paymentRepository.existsByPaymentKey(request.paymentKey())) {
            throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
        }

        // 2. Toss 결제 승인 API 호출 (RestTemplate 사용)
        TossPaymentConfirmResponse tossResponse = tossPaymentClient.requestPaymentConfirm(request);

        // 추후 paymentKey로 조회해 실제 결제 방식에 따라 업데이트하는 방식으로 리팩토링 예정
        PaymentMethod method = PaymentMethod.CARD;
        // // 3. Toss method 문자열을 enum으로 변환
        //PaymentMethod method = PaymentMethodMapper.fromToss(tossResponse);


        // 4. 오늘 날짜 기준 생성된 결제 수 조회하여 시퀀스 결정 (paymentNo 생성용)
        LocalDate today = LocalDate.now();
        int todayCount = paymentRepository.countByCreatedAtBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );

        // 5. 고유 paymentNo 생성
        String paymentNo = PaymentNoGenerator.generate(today, todayCount + 1);

        // 6. Toss 응답값을 Payment 엔티티로 변환하여 저장
        Payment payment = Payment.builder()
            .paymentNo(paymentNo)
            .tossOrderId(request.orderId())
            .paymentKey(tossResponse.paymentKey())
            .amount(tossResponse.totalAmount())
            .currency("KRW")
            .method(method)
            .paidAt(OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.status()))
            .build();

        paymentRepository.save(payment);

        // 7. 응답 DTO 반환
        return PaymentConfirmResponse.convertFromTossConfirm(tossResponse, method);
    }

    // 단건 결제 조회 API (orderId)
    public PaymentLookupResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByTossOrderId(orderId)
            .orElseThrow(() -> new ApiException("해당 주문의 결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        TossSinglePaymentLookupResponse tossResponse = tossPaymentClient.requestSinglePaymentLookup(orderId);

        return PaymentLookupResponse.convertFromTossLookup(tossResponse);
    }

}
