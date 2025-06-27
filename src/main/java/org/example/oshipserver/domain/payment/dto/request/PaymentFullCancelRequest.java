package org.example.oshipserver.domain.payment.dto.request;

public record PaymentFullCancelRequest(
    String cancelReason
) {}
