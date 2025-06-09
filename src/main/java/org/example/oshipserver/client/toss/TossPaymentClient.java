package org.example.oshipserver.client.toss;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossSinglePaymentLookupResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentClient { // 외부 연동 모듈

    @Qualifier("tossRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    // 요청 url 생성
    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_LOOKUP_URL_PREFIX = "https://api.tosspayments.com/v1/payments/";

    /**
     * Toss 단건 결제 승인 요청
     */
    public TossPaymentConfirmResponse requestPaymentConfirm(PaymentConfirmRequest request) {
        // 인증 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes())
        );

        // 요청 바디 구성
        Map<String, Object> body = Map.of(
            "paymentKey", request.paymentKey(),
            "orderId", request.orderId(),
            "amount", request.amount(),
            "currency", "KRW"
        );

        // 바디와 헤더를 하나로 묶은 HTTP 요청 객체
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // RestTemplate으로 POST 요청
        ResponseEntity<TossPaymentConfirmResponse> response = restTemplate.exchange(
            TOSS_CONFIRM_URL,  // 재사용성을 위해 상수로 선언
            HttpMethod.POST,
            entity,
            TossPaymentConfirmResponse.class
        );

        // 응답 추출(토스 서버 응답을 TossPaymentConfirmResponse로 역직렬화)
        return response.getBody();
    }


    /**
     * Toss 단건 결제 조회 요청
     */
    public TossSinglePaymentLookupResponse requestSinglePaymentLookup(String paymentKey) {
        String url = TOSS_LOOKUP_URL_PREFIX + paymentKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes())
        );

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TossSinglePaymentLookupResponse> response = restTemplate.exchange(
            url,  // url을 동적으로 조립
            HttpMethod.GET,
            entity,
            TossSinglePaymentLookupResponse.class
        );

        return response.getBody();
    }
}