package org.example.oshipserver.domain.payment.mapper;

import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public class PaymentMethodMapper {

    public static PaymentMethod fromToss(TossPaymentConfirmResponse response) {
        String tossMethod = response.method();

        if ("간편결제".equals(tossMethod)) {
            if (response.card() != null) {
                return PaymentMethod.SIMPLE_PAY_CARD;
            } else if (response.easyPay() != null && "토스페이".equals(response.easyPay().provider())) {
                return PaymentMethod.SIMPLE_PAY_TOSS_MONEY;
            }
        }

        throw new ApiException("지원하지 않는 결제 방식입니다.", ErrorType.INVALID_PAYMENT_METHOD);
    }
}