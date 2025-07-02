package org.example.oshipserver.domain.payment.mapper;

import static org.assertj.core.api.Assertions.*;

import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse.Card;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse.EasyPay;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse.Transfer;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentMethodMapperTest {

    @Test
    @DisplayName("간편결제 카드 : 토스페이 + 카드 정보 존재 시 EASY_PAY_CARD 반환")
    void fromToss_간편결제_카드() {
        TossPaymentConfirmResponse res = new TossPaymentConfirmResponse();
        res.setMethod("EASY_PAY");

        EasyPay easyPay = new EasyPay();
        easyPay.setProvider("토스페이");
        res.setEasyPay(easyPay);

        Card card = new Card();
        card.setNumber("1234567890123456");
        res.setCard(card);

        PaymentMethod method = PaymentMethodMapper.fromToss(res);
        assertThat(method).isEqualTo(PaymentMethod.EASY_PAY_CARD);
    }

    @Test
    @DisplayName("간편결제 계좌 : 토스페이 + transfer 정보 존재 시 EASY_PAY_ACCOUNT 반환")
    void fromToss_간편결제_계좌() {
        TossPaymentConfirmResponse res = new TossPaymentConfirmResponse();
        res.setMethod("EASY_PAY");

        EasyPay easyPay = new EasyPay();
        easyPay.setProvider("토스페이");
        res.setEasyPay(easyPay);

        Transfer transfer = new Transfer();
        transfer.setAccountNumber("110123456789");
        res.setTransfer(transfer);

        PaymentMethod method = PaymentMethodMapper.fromToss(res);
        assertThat(method).isEqualTo(PaymentMethod.EASY_PAY_ACCOUNT);
    }

    @Test
    @DisplayName("일반 카드 결제 - method가 CARD일 경우 CARD 반환")
    void fromToss_일반_카드() {
        TossPaymentConfirmResponse res = new TossPaymentConfirmResponse();
        res.setMethod("CARD");

        PaymentMethod method = PaymentMethodMapper.fromToss(res);
        assertThat(method).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    @DisplayName("일반 계좌이체 - method가 TRANSFER일 경우 TRANSFER 반환")
    void fromToss_일반_계좌이체() {
        TossPaymentConfirmResponse res = new TossPaymentConfirmResponse();
        res.setMethod("TRANSFER");

        PaymentMethod method = PaymentMethodMapper.fromToss(res);
        assertThat(method).isEqualTo(PaymentMethod.TRANSFER);
    }

    @Test
    @DisplayName("잘못된 결제 방식일 경우 ApiException 발생")
    void fromToss_지원하지않는방식() {
        TossPaymentConfirmResponse res = new TossPaymentConfirmResponse();
        res.setMethod("BITCOIN");

        ApiException ex = catchThrowableOfType(
            () -> PaymentMethodMapper.fromToss(res),
            ApiException.class
        );

        assertThat(ex).isNotNull();
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.INVALID_PAYMENT_METHOD);
    }
}
