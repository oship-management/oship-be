package org.example.oshipserver.domain.payment.dto.request;

public record PaymentCancelRequest(
    String cancelReason,
    Integer cancelAmount  // null이면 전체 취소
) {}
