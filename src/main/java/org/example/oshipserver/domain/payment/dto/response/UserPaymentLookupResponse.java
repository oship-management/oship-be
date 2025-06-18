package org.example.oshipserver.domain.payment.dto.response;

import java.util.List;
import org.example.oshipserver.domain.order.dto.response.OrderPaymentResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import java.time.LocalDateTime;

/**
 * 사용자용 (민감한 정보 포함x. tossOrderId, paymentKey)
 * 결제 조회 응답 DTO
 */
public record UserPaymentLookupResponse(
    String paymentStatus,
    String paidAt,
    Integer amount,
    String currency,
    String cardLast4Digits,
    String receiptUrl,
    List<OrderPaymentResponse> orders
) {
    public static UserPaymentLookupResponse fromPaymentEntityForUser(
        Payment payment,
        List<OrderPaymentResponse> orders
    ) {
        return new UserPaymentLookupResponse(
            payment.getStatus().name(),
            payment.getPaidAt() != null ? payment.getPaidAt().toString() : null,
            payment.getAmount(),
            payment.getCurrency(),
            payment.getCardLast4Digits(),
            payment.getReceiptUrl(),
            orders
        );
    }
}
