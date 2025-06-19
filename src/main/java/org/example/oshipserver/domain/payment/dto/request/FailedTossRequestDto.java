package org.example.oshipserver.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Toss 실패 요청 큐 적재용 DTO
 */
public record FailedTossRequestDto(
    String url,
    Map<String, Object> body,
    String idempotencyKey
) {
    @JsonCreator
    public FailedTossRequestDto(
        @JsonProperty("url") String url,
        @JsonProperty("body") Map<String, Object> body,
        @JsonProperty("idempotencyKey") String idempotencyKey
    ) {
        this.url = url;
        this.body = body;
        this.idempotencyKey = idempotencyKey;
    }

    // Toss 결제 승인 요청용 DTO로 변환
    public PaymentConfirmRequest toConfirmRequest() {
        return new PaymentConfirmRequest(
            (String) body.get("paymentKey"),
            Long.valueOf((String) body.get("orderId")),
            (String) body.get("orderId"),
            (Integer) body.get("amount")
        );
    }
}
