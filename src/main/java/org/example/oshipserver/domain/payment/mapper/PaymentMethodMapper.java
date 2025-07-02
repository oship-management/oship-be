package org.example.oshipserver.domain.payment.mapper;

import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public class PaymentMethodMapper {

    public static PaymentMethod fromToss(TossPaymentConfirmResponse response) {
        String method = response.getMethod();

        if ("EASY_PAY".equals(method)) {
            // 간편결제: 실제 결제 수단을 card / transfer로 구분
            if (response.getEasyPay() != null && "토스페이".equals(response.getEasyPay().getProvider())) {
                if (response.getCard() != null) {
                    return PaymentMethod.EASY_PAY_CARD;
                } else if (response.getTransfer() != null) {
                    return PaymentMethod.EASY_PAY_ACCOUNT;
                }
            }
        }

        // 일반 결제 방식
        if ("CARD".equals(method)) {
            return PaymentMethod.CARD;
        }

        if ("TRANSFER".equals(method)) {
            return PaymentMethod.TRANSFER;
        }

        throw new ApiException("지원하지 않는 결제 방식입니다.", ErrorType.INVALID_PAYMENT_METHOD);
    }
}