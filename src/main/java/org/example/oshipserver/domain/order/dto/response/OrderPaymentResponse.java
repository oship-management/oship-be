package org.example.oshipserver.domain.order.dto.response;

import java.math.BigDecimal;
import org.example.oshipserver.domain.order.entity.Order;

/**
 * payment 조회시 연관된 order 함께 보여주기 위한 dto
 */
public record OrderPaymentResponse(
    Long orderId,
    String orderNo,
    Integer parcelCount,
    BigDecimal shipmentActualWeight,
    String orderStatus,
    String senderName,
    String recipientName,
    Integer orderAmount,   // 주문별 결제 금액
    String oshipMasterNo
) {
    public static OrderPaymentResponse from(Order order, Integer paymentAmount) {
        return new OrderPaymentResponse(
            order.getId(),
            order.getOrderNo(),
            order.getParcelCount(),
            order.getShipmentActualWeight(),
            order.getCurrentStatus().name(),
            order.getSender() != null ? order.getSender().getSenderName() : null,
            order.getRecipient() != null ? order.getRecipient().getRecipientName() : null,
            paymentAmount,
            order.getOshipMasterNo()
        );
    }
}