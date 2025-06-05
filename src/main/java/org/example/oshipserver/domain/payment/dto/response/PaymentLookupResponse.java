package org.example.oshipserver.domain.payment.dto.response;

import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;

/**
 * 결제 단건 조회 응답 DTO (내부 응답용)
 */
public record PaymentLookupResponse(
    String tossOrderId,
    String paymentKey,
    PaymentStatus paymentStatus,
    String paidAt,
    Integer amount,
    String currency,
    String cardLast4Digits,
    String receiptUrl
) {

    /**
     * Toss 단건 결제 조회 응답을 내부 응답 DTO 변환
     */
    public static PaymentLookupResponse convertFromTossLookup(
        TossSinglePaymentLookupResponse response) {
        return new PaymentLookupResponse(
            response.orderId(), // Toss 응답의 orderId를 tossOrderId로 매핑
            response.paymentKey(),
            PaymentStatusMapper.fromToss(response.status()),
            response.approvedAt(),
            response.totalAmount(),
            response.currency(),
            getLast4Digits(response.card() != null ? response.card().number() : null),
            response.receipt() != null ? response.receipt().url() : null
        );
    }

    /**
     * 카드 번호에서 마지막 4자리 추출
     */
    private static String getLast4Digits(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }
}
