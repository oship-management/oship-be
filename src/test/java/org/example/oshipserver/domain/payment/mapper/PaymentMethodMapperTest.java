import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse.Card;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse.Transfer;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse.EasyPay;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.mapper.PaymentMethodMapper;
import org.example.oshipserver.global.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentMethodMapperTest {

    @Test
    @DisplayName("카드 결제는 CARD로 매핑된다")
    void 카드_결제() {
        TossPaymentConfirmResponse response = new TossPaymentConfirmResponse();
        response.setMethod("카드");

        PaymentMethod result = PaymentMethodMapper.fromToss(response);

        assertThat(result).isEqualTo(PaymentMethod.CARD);
    }

    @Test
    @DisplayName("계좌이체 결제는 TRANSFER로 매핑된다")
    void 계좌이체_결제() {
        TossPaymentConfirmResponse response = new TossPaymentConfirmResponse();
        response.setMethod("계좌이체");

        PaymentMethod result = PaymentMethodMapper.fromToss(response);

        assertThat(result).isEqualTo(PaymentMethod.TRANSFER);
    }

    @Test
    @DisplayName("간편결제 + 토스페이 + 카드가 있을 경우 EASY_PAY_CARD로 매핑된다")
    void 간편결제_카드() {
        TossPaymentConfirmResponse response = new TossPaymentConfirmResponse();
        response.setMethod("간편결제");

        EasyPay easyPay = new EasyPay();
        easyPay.setProvider("토스페이");
        response.setEasyPay(easyPay);

        Card card = new Card();
        card.setNumber("1234567812345678");
        response.setCard(card);

        PaymentMethod result = PaymentMethodMapper.fromToss(response);

        assertThat(result).isEqualTo(PaymentMethod.EASY_PAY_CARD);
    }

    @Test
    @DisplayName("간편결제 + 토스페이 + 계좌이체가 있을 경우 EASY_PAY_ACCOUNT로 매핑된다")
    void 간편결제_계좌이체() {
        TossPaymentConfirmResponse response = new TossPaymentConfirmResponse();
        response.setMethod("간편결제");

        EasyPay easyPay = new EasyPay();
        easyPay.setProvider("토스페이");
        response.setEasyPay(easyPay);

        Transfer transfer = new Transfer();
        transfer.setBankName("하나은행");
        response.setTransfer(transfer);

        PaymentMethod result = PaymentMethodMapper.fromToss(response);

        assertThat(result).isEqualTo(PaymentMethod.EASY_PAY_ACCOUNT);
    }

    @Test
    @DisplayName("간편결제 + 토스페이 + card/transfer 모두 없을 경우 기본적으로 EASY_PAY_CARD로 매핑된다")
    void 간편결제_정보없을때_기본카드() {
        TossPaymentConfirmResponse response = new TossPaymentConfirmResponse();
        response.setMethod("간편결제");

        EasyPay easyPay = new EasyPay();
        easyPay.setProvider("토스페이");
        response.setEasyPay(easyPay);

        response.setCard(null);
        response.setTransfer(null);

        PaymentMethod result = PaymentMethodMapper.fromToss(response);

        assertThat(result).isEqualTo(PaymentMethod.EASY_PAY_CARD);
    }

    @Test
    @DisplayName("지원하지 않는 결제 방식일 경우 예외 발생")
    void 잘못된_결제방식() {
        TossPaymentConfirmResponse response = new TossPaymentConfirmResponse();
        response.setMethod("현금결제");

        assertThatThrownBy(() -> PaymentMethodMapper.fromToss(response))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("지원하지 않는 결제 방식입니다.");
    }
}
