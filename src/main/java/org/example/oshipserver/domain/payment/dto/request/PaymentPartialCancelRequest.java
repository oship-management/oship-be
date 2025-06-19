package org.example.oshipserver.domain.payment.dto.request;

public record PaymentPartialCancelRequest(
    Long orderId,
    Integer cancelAmount,
    String cancelReason
) {}
