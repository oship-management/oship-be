package org.example.oshipserver.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;

/**
 * 단건 결제 생성 응답 DTO (내부 응답용)
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 값은 json 응답에서 제외 (토스 간편결제시 카드 뒷자리가 null로 들어옴)
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
     * Toss 기준 단건 결제 승인 응답을 내부 응답 DTO로 변환
     */
    public static PaymentConfirmResponse convertFromTossConfirm(
        TossPaymentConfirmResponse response,
        PaymentMethod method) {
        return new PaymentConfirmResponse(
            response.getOrderId(),
            response.getPaymentKey(),
            PaymentStatusMapper.fromToss(response.getStatus()),
            response.getApprovedAt(),
            response.getTotalAmount(),
            response.getCurrency(),
            method,
            (method == PaymentMethod.EASY_PAY_CARD || method == PaymentMethod.CARD) && response.getCard() != null

                ? getLast4Digits(response.getCard().getNumber())  // 결제방법이 카드일때만 카드4자리 보여줌
                : null,
            response.getReceipt() != null ? response.getReceipt().getUrl() : null
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