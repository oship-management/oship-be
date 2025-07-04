package org.example.oshipserver.domain.payment.dto.response;

import java.util.List;
import java.util.Map;
import org.example.oshipserver.domain.order.dto.response.OrderPaymentResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;
import org.example.oshipserver.domain.order.entity.Order;

/**
 * 관리자 페이지용 (tossOrderId, paymentKey 포함)
 * 결제 조회 응답 DTO (내부 응답용)
 */
public record PaymentLookupResponse(
    Long paymentId,
    String tossOrderId,
    String paymentKey,
    PaymentStatus paymentStatus,
    String paidAt,
    Integer amount,
    String currency,
    String cardLast4Digits,
    String receiptUrl,
    List<OrderPaymentResponse> orders
) {

//    /**
//     * Toss 기준 단건 결제 조회 응답을 내부 응답 DTO로 변환
//     * toss에 직접 조회 요청했을 때 사용
//     */
//    public static PaymentLookupResponse convertFromTossLookup(
//        TossSinglePaymentLookupResponse response) {
//        return new PaymentLookupResponse(
//            response.orderId(), // Toss 응답의 orderId를 tossOrderId로 매핑
//            response.paymentKey(),
//            PaymentStatusMapper.fromToss(response.status()),
//            response.approvedAt(),
//            response.totalAmount(),
//            response.currency(),
//            getLast4Digits(response.card() != null ? response.card().number() : null),
//            response.receipt() != null ? response.receipt().url() : null,
//            List.of()
//        );
//    }

//    public static PaymentLookupResponse fromPaymentAndOrders(Payment payment, List<Order> orders) {
//        return new PaymentLookupResponse(
//            payment.getTossOrderId(),
//            payment.getPaymentKey(),
//            payment.getStatus(),
//            payment.getPaidAt().toString(),
//            payment.getAmount(),
//            payment.getCurrency(),
//            payment.getCardLast4Digits(),
//            payment.getReceiptUrl()
//        );
//    }

    public static PaymentLookupResponse fromPaymentEntity(
        Payment payment,
        List<Order> orders,
        Map<Long, Integer> orderAmounts
    ) {
        List<OrderPaymentResponse> orderResponses = orders.stream()
            .map(order -> OrderPaymentResponse.from(order, orderAmounts.get(order.getId())))
            .toList();

        return new PaymentLookupResponse(
            payment.getId(),
            payment.getTossOrderId(),
            payment.getPaymentKey(),
            payment.getStatus(),
            payment.getPaidAt() != null ? payment.getPaidAt().toString() : null,
            payment.getAmount(),
            payment.getCurrency(),
            payment.getCardLast4Digits()!= null ? payment.getCardLast4Digits() : null,
            payment.getReceiptUrl(),
            orderResponses
        );
    }

    /**
     * 카드 번호에서 마지막 4자리 추출
     */
    private static String getLast4Digits(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }
}
