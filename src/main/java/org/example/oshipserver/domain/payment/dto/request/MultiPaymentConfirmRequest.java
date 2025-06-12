package org.example.oshipserver.domain.payment.dto.request;

import java.util.List;

/**
 * 다건 결제 승인 요청 DTO (Toss 결제 승인 API 요청용)
 */
public record MultiPaymentConfirmRequest(
    String paymentKey,
    String tossOrderId,
    List<MultiOrderRequest> orders,
    String currency
) {
    public record MultiOrderRequest(Long orderId, Integer amount) {}
}
