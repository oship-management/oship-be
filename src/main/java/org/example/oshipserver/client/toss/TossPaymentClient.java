package org.example.oshipserver.client.toss;

import jakarta.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.payment.dto.request.FailedTossRequestDto;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient { // 외부 연동 모듈

    // 공통 멱등성 요청 로직을 가진 컴포넌트
    private final IdempotentRestClient idempotentRestClient;

    // RestTemplate 빈
    @Qualifier("tossRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    // 요청 url 생성
    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_LOOKUP_URL_PREFIX = "https://api.tosspayments.com/v1/payments/";
    private static final String TOSS_CANCEL_URL_PREFIX = "https://api.tosspayments.com/v1/payments/";

    /**
     * Toss 결제 승인 요청
     */
    public TossPaymentConfirmResponse requestPaymentConfirm(PaymentConfirmRequest request, String idempotencyKey) {
        Map<String, Object> body = Map.of(
            "paymentKey", request.paymentKey(),
            "orderId", request.tossOrderId(),
            "amount", request.amount(),
            "currency", "KRW"
        );

        // 공통 멱등성 클라이언트를 통해 post 요청
        return idempotentRestClient.postForIdempotent(
            TOSS_CONFIRM_URL,
            body,
            TossPaymentConfirmResponse.class,
            idempotencyKey
        );
    }


    /**
     * Toss 단건 결제 조회 요청
     */
    public TossSinglePaymentLookupResponse requestSinglePaymentLookup(String paymentKey) {
        String url = TOSS_LOOKUP_URL_PREFIX + paymentKey;

        // Authorization 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes())
        );

        // GET 요청을 위한 HttpEntity 생성
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // RestTemplate을 통해 toss api에 get 요청
        ResponseEntity<TossSinglePaymentLookupResponse> response = restTemplate.exchange(
            url,  // url을 동적으로 조립
            HttpMethod.GET,
            entity,
            TossSinglePaymentLookupResponse.class
        );

        return response.getBody();
    }

    /**
     * Toss 결제 취소 요청
     */
    public void requestCancel(String paymentKey, String cancelReason, @Nullable Integer cancelAmount) {
        String url = TOSS_CANCEL_URL_PREFIX + paymentKey + "/cancel";

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8)));

        // 요청 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", cancelReason);
        if (cancelAmount != null) {
            body.put("cancelAmount", cancelAmount);
        }

        // Toss로 POST 요청 실행
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class); // Toss가 우리 서버에 주는 응답바디 없음
    }

    /**
     * Redis에 적재된 실패 요청 기반으로 Toss 결제 승인 요청 (재시도)
     */
    public TossPaymentConfirmResponse retryPaymentConfirm(FailedTossRequestDto dto) {
        log.info("Toss 재시도 요청 시작: {}", dto.idempotencyKey());
        return requestPaymentConfirm(dto.toConfirmRequest(), dto.idempotencyKey());
    }

}