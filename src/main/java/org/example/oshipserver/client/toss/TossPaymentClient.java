package org.example.oshipserver.client.toss;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Qualifier("tossRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    public TossPaymentConfirmResponse requestPaymentConfirm(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes())
        );

        Map<String, Object> body = Map.of(
            "paymentKey", request.paymentKey(),
            "orderId", request.orderId(),
            "amount", request.amount(),
            "currency", "KRW"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TossPaymentConfirmResponse> response = restTemplate.exchange(
            TOSS_CONFIRM_URL,
            HttpMethod.POST,
            entity,
            TossPaymentConfirmResponse.class
        );

        return response.getBody();
    }
}
