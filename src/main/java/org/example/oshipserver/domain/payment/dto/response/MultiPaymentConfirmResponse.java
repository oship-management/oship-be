package org.example.oshipserver.domain.payment.dto.response;

import java.util.List;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;


/**
 * 다건 결제 생성 응답 DTO (내부 응답용)
 */
public record MultiPaymentConfirmResponse(
    List<String> orderIds,
    String paymentKey,
    PaymentStatus paymentStatus,
    String approvedAt,
    Integer totalAmount,
    String currency,
    String cardLast4Digits,
    String receiptUrl
) {

    /**
     * Toss 다건 결제 승인 응답을 내부 응답 DTO 변환
     */
    public static MultiPaymentConfirmResponse convertFromTossConfirm(
        TossPaymentConfirmResponse response,
        List<String> orderIds
    ) {
        return new MultiPaymentConfirmResponse(
            orderIds,
            response.paymentKey(),
            PaymentStatusMapper.fromToss(response.status()),
            response.approvedAt(),
            response.totalAmount(),
            response.currency(),
            response.card() != null ? getLast4Digits(response.card().number()) : null,
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