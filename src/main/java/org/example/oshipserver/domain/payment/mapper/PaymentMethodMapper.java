package org.example.oshipserver.domain.payment.mapper;

import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public class PaymentMethodMapper {

    public static PaymentMethod fromToss(TossPaymentConfirmResponse response) {
        String method = response.method();

        // 간편결제(easypay): 토스페이
        if ("간편결제".equals(method)) {
            if (response.easyPay() != null && "토스페이".equals(response.easyPay().provider())) {  // provider가 토스페이인지 꼭 체크
                if (response.card() != null) {  // 카드값 null여부로 카드/계좌 구분함
                    return PaymentMethod.EASY_PAY_CARD; // 토스 카드 결제
                } else {
                    return PaymentMethod.EASY_PAY_ACCOUNT; // 토스 계좌 결제
                }
            }
        }

        throw new ApiException("지원하지 않는 결제 방식입니다.", ErrorType.INVALID_PAYMENT_METHOD);
    }
}