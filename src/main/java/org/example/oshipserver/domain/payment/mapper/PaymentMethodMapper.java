package org.example.oshipserver.domain.payment.mapper;

import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public class PaymentMethodMapper {

    public static PaymentMethod fromToss(TossPaymentConfirmResponse response) {
        String method = response.getMethod(); // Toss 응답은 한글로 "간편결제", "카드", "계좌이체" 등을 보냄

        // 간편결제 처리
        if ("간편결제".equals(method)) {
            if (response.getEasyPay() != null && "토스페이".equals(response.getEasyPay().getProvider())) {
                // 카드/계좌이체 구분
                if (response.getCard() != null) {
                    return PaymentMethod.EASY_PAY_CARD;
                } else if (response.getTransfer() != null) {
                    return PaymentMethod.EASY_PAY_ACCOUNT;
                } else {
                    // 둘 다 없는 경우 기본적으로 카드로 간주
                    return PaymentMethod.EASY_PAY_CARD;
                }
            }
        }

        // 일반 결제 방식
        if ("카드".equals(method)) {
            return PaymentMethod.CARD;
        }

        if ("계좌이체".equals(method)) {
            return PaymentMethod.TRANSFER;
        }

        throw new ApiException("지원하지 않는 결제 방식입니다.", ErrorType.INVALID_PAYMENT_METHOD);
    }
}
