package org.example.oshipserver.domain.payment.dto.response;

/**
 * Toss 단건 결제 승인 응답 전체 DTO
 */
public record TossPaymentConfirmResponse(
    String mId,
    String lastTransactionKey,
    String paymentKey,
    String orderId,
    String orderName,
    int taxExemptionAmount,
    String status,
    String requestedAt,
    String approvedAt,
    boolean useEscrow,
    boolean cultureExpense,
    Card card,
    String type,
    EasyPay easyPay,
    String country,
    Failure failure,
    boolean isPartialCancelable,
    Receipt receipt,
    Checkout checkout,
    String currency,
    int totalAmount,
    int balanceAmount,
    int suppliedAmount,
    int vat,
    int taxFreeAmount,
    String method,
    String version
) {
    public record Card(
        String issuerCode,
        String acquirerCode,
        String number,
        int installmentPlanMonths,
        boolean isInterestFree,
        String approveNo,
        boolean useCardPoint,
        String cardType,
        String ownerType,
        String acquireStatus,
        int amount
    ) {}

    public record EasyPay(
        String provider,
        int amount,
        int discountAmount
    ) {}

    public record Failure(
        String code,
        String message
    ) {}

    public record Receipt(
        String url
    ) {}

    public record Checkout(
        String url
    ) {}
}