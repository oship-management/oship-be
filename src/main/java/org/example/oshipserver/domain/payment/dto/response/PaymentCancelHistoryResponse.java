package org.example.oshipserver.domain.payment.dto.response;

import java.time.LocalDateTime;
import org.example.oshipserver.domain.payment.entity.PaymentCancelHistory;

public record PaymentCancelHistoryResponse(
    int cancelAmount,
    String cancelReason,
    LocalDateTime canceledAt
) {
    public static PaymentCancelHistoryResponse fromEntity(PaymentCancelHistory history) {
        return new PaymentCancelHistoryResponse(
            history.getCancelAmount(),
            history.getCancelReason(),
            history.getCanceledAt()
        );
    }
}

