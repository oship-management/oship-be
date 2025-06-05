package org.example.oshipserver.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oshipserver.domain.order.entity.Order;

public record OrderDetailResponse(
    Long id,
    String orderNo,
    String storePlatform,
    String storeName,
    String shipmentStatus,
    String orderStatus,
    LocalDateTime createdAt,
    int parcelCount,
    BigDecimal shipmentActualWeight,
    BigDecimal shipmentVolumeWeight,
    String weightUnit,
    String shippingTerm,
    boolean isPrintBarcode,
    boolean isPrintAwb,
    Integer deliveryDays,
    String oshipMasterNo,
    Dimension dimension,
    Sender sender,
    Recipient recipient,
    List<Item> items
) {

    public record Dimension(
        BigDecimal width,
        BigDecimal length,
        BigDecimal height
    ) {}

    public record Sender(
        String name,
        String company,
        String email,
        String phoneNo,
        Address address
    ) {}

    public record Recipient(
        String name,
        String company,
        String email,
        String phoneNo,
        String taxId,
        Address address
    ) {}

    public record Address(
        String countryCode,
        String state,
        String stateCode,
        String city,
        String address1,
        String address2,
        String zipCode
    ) {}

    public record Item(
        Long id,
        String name,
        int quantity,
        BigDecimal unitValue,
        String valueCurrency,
        BigDecimal weight,
        String weightUnit,
        String itemHsCode,
        String itemOriginCountryCode
    ) {}

    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getSender().getStorePlatform(),
            order.getSender().getStoreName(),
            order.getLastTrackingEvent(),
            order.getCurrentStatus().name(),
            order.getCreatedAt(),
            order.getParcelCount(),
            order.getShipmentActualWeight(),
            order.getShipmentVolumeWeight(),
            order.getWeightUnit(),
            order.getShippingTerm(),
            Boolean.TRUE.equals(order.getIsPrintBarcode()),
            Boolean.TRUE.equals(order.getIsPrintAwb()),
            order.getDeliveryDays(),
            order.getOshipMasterNo(),
            new Dimension(
                order.getDimensionWidth(),
                order.getDimensionLength(),
                order.getDimensionHeight()
            ),
            new Sender(
                order.getSender().getSenderName(),
                order.getSender().getSenderCompany(),
                order.getSender().getSenderEmail(),
                order.getSender().getSenderPhoneNo(),
                new Address(
                    order.getSender().getSenderAddress().getSenderCountryCode().name(),
                    order.getSender().getSenderAddress().getSenderState(),
                    order.getSender().getSenderAddress().getSenderStateCode().name(),
                    order.getSender().getSenderAddress().getSenderCity(),
                    order.getSender().getSenderAddress().getSenderAddress1(),
                    order.getSender().getSenderAddress().getSenderAddress2(),
                    order.getSender().getSenderAddress().getSenderZipCode()
                )
            ),
            new Recipient(
                order.getRecipient().getRecipientName(),
                order.getRecipient().getRecipientCompany(),
                order.getRecipient().getRecipientEmail(),
                order.getRecipient().getRecipientPhoneNo(),
                order.getRecipient().getRecipientAddress().getRecipientTaxId(),
                new Address(
                    order.getRecipient().getRecipientAddress().getRecipientCountryCode().name(),
                    order.getRecipient().getRecipientAddress().getRecipientState(),
                    order.getRecipient().getRecipientAddress().getRecipientStateCode().name(),
                    order.getRecipient().getRecipientAddress().getRecipientCity(),
                    order.getRecipient().getRecipientAddress().getRecipientAddress1(),
                    order.getRecipient().getRecipientAddress().getRecipientAddress2(),
                    order.getRecipient().getRecipientAddress().getRecipientZipCode()
                )
            ),
            order.getOrderItems().stream()
                .map(item -> new Item(
                    item.getId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getUnitValue(),
                    item.getValueCurrency(),
                    item.getWeight(),
                    item.getWeightUnit(),
                    item.getHsCode(),
                    item.getOriginCountryCode()
                ))
                .toList()
        );
    }
}
