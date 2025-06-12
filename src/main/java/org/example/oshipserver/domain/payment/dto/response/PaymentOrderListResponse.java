package org.example.oshipserver.domain.payment.dto.response;

import java.math.BigDecimal;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;

public record PaymentOrderListResponse(
    Long orderId,
    String orderNo,
    int parcelCount,
    BigDecimal shipmentActualWeight,
    OrderStatus status,
    String senderName,
    String recipientName
) {
    public static PaymentOrderListResponse from(Order order) {
        return new PaymentOrderListResponse(
            order.getId(),
            order.getOrderNo(),
            order.getParcelCount(),
            order.getShipmentActualWeight(),
            order.getCurrentStatus(),
            order.getSender() != null ? order.getSender().getSenderName() : null,
            order.getRecipient() != null ? order.getRecipient().getRecipientName() : null
        );
    }
}
