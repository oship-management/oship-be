package org.example.oshipserver.domain.payment.dto.response;

import java.time.LocalDateTime;
import org.example.oshipserver.domain.payment.entity.PaymentCancelHistory;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;

public record PaymentCancelHistoryResponse(
    Long orderId,
    Integer cancelAmount,
    String cancelReason,
    LocalDateTime canceledAt,
    PaymentStatus paymentStatus
) {
    public static PaymentCancelHistoryResponse fromEntity(PaymentCancelHistory history) {
        return new PaymentCancelHistoryResponse(
            history.getPaymentOrder() != null ? history.getPaymentOrder().getOrder().getId() : null,
            history.getCancelAmount(),
            history.getCancelReason(),
            history.getCanceledAt(),
            history.getPaymentOrder().getPaymentStatus()
        );
    }
}

