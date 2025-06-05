package org.example.oshipserver.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oshipserver.domain.order.entity.Order;

public record OrderListResponse(
    Long id,
    String orderNo,
    String oshopMasterNo,
    String storePlatform,
    String storeName,
    LocalDateTime createdAt,
    int parcelCount,
    BigDecimal shipmentActualWeight,
    BigDecimal shipmentVolumeWeight,
    String senderName,
    String recipientName,
    Integer deliveryDays,
    String lastTrackingEvent,
    String orderStatus,
    boolean isPrintBarcode,
    boolean isPrintAwb
) {
    public static OrderListResponse from(Order order) {
        return new OrderListResponse(
            order.getId(),
            order.getOrderNo(),
            order.getOshipMasterNo(),
            order.getSender() != null ? order.getSender().getStorePlatform() : null,
            order.getSender() != null ? order.getSender().getStoreName() : null,
            order.getCreatedAt(),
            order.getParcelCount(),
            order.getShipmentActualWeight(),
            order.getShipmentVolumeWeight(),
            order.getSender() != null ? order.getSender().getSenderName() : null,
            order.getRecipient() != null ? order.getRecipient().getRecipientName() : null,
            order.getDeliveryDays(),
            order.getLastTrackingEvent(),
            order.getCurrentStatus() != null ? order.getCurrentStatus().name() : null,
            Boolean.TRUE.equals(order.getIsPrintBarcode()),
            Boolean.TRUE.equals(order.getIsPrintAwb())
        );
    }
}
