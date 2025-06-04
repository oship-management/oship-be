package org.example.oshipserver.domain.payment.mapper;

import org.example.oshipserver.domain.payment.entity.PaymentStatus;

public class PaymentStatusMapper {

    public static PaymentStatus fromToss(String tossStatus) {
        return switch (tossStatus) {
            case "DONE" -> PaymentStatus.COMPLETE;
            case "CANCELED" -> PaymentStatus.CANCEL;
            case "PARTIAL_CANCELED" -> PaymentStatus.PARTIAL_CANCEL;
            case "WAITING_FOR_APPROVAL" -> PaymentStatus.WAIT;
            case "FAILED" -> PaymentStatus.FAIL;
            case "ABORTED" -> PaymentStatus.WAIT_CANCEL;
            default -> PaymentStatus.NONE;
        };
    }

    public static PaymentStatus fromKakao(String kakaoStatus) {
        return switch (kakaoStatus) {
            case "APPROVED" -> PaymentStatus.COMPLETE;
            case "CANCELLED" -> PaymentStatus.CANCEL;
            case "FAILED" -> PaymentStatus.FAIL;
            default -> PaymentStatus.NONE;
        };
    }
}