package org.example.oshipserver.domain.payment.dto.response;

/**
 * Toss 단건 결제 조회 응답 전체 DTO
 */
public record TossSinglePaymentLookupResponseDto(
    String paymentKey,
    String orderId,
    String status,
    String approvedAt,
    Integer totalAmount,
    String currency,
    Card card,
    Receipt receipt
) {
    public record Card(
        String number
    ) {}

    public record Receipt(
        String url
    ) {}
}
