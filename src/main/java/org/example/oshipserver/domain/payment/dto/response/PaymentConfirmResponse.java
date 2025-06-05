package org.example.oshipserver.domain.payment.dto.response;

import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;

/**
 * 단건 결제 생성 응답 DTO (내부 응답용)
 */
public record PaymentConfirmResponse(
    String tossOrderId,           // 주문 번호
    String paymentKey,        // Toss 결제 키
    PaymentStatus status,     // 결제 상태 (enum 매핑)
    String approvedAt,        // 승인 완료 시간
    Integer amount,           // 결제 금액
    String currency,          // 통화 단위
    PaymentMethod method,     // 결제 방식
    String cardLast4Digits,   // 카드 마지막 4자리
    String receiptUrl         // 영수증 URL
) {

    /**
     * Toss 단건 결제 승인 응답을 내부 응답 DTO 변환
     */
    public static PaymentConfirmResponse convertFromTossConfirm(
        TossPaymentConfirmResponse response,
        PaymentMethod method) {
        return new PaymentConfirmResponse(
            response.orderId(),
            response.paymentKey(),
            PaymentStatusMapper.fromToss(response.status()),
            response.approvedAt(),
            response.totalAmount(),
            response.currency(),
            method,
            method == PaymentMethod.CARD && response.card() != null
                ? getLast4Digits(response.card().number())  // 결제방법이 카드일때만 카드4자리 보여줌
                : null,
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