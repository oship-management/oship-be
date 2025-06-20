package org.example.oshipserver.domain.order.service.bulkmapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.example.oshipserver.domain.order.dto.bulk.OrderBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderItemBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderRecipientBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderSenderBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.RecipientAddressBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.SenderAddressBulkDto;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapperImpl implements OrderDtoMapper {

    @Override
    public OrderBulkDto toOrderDto(OrderCreateRequest req, String masterNo, Long sellerId) {
        return new OrderBulkDto(
            req.orderNo(), masterNo,
            req.shippingTerm(), req.serviceType(), req.weightUnit(),
            req.shipmentActualWeight(), req.shipmentVolumeWeight(),
            (double) req.dimensionWidth(), (double) req.dimensionLength(), (double) req.dimensionHeight(),
            req.packageType(), req.parcelCount(), req.itemContentsType(),
            false, LocalDateTime.now(), LocalDateTime.now(), sellerId, req.lastTrackingEvent()
        );
    }

    @Override
    public List<OrderItemBulkDto> toOrderItemDtos(OrderCreateRequest req, String masterNo, Long orderId) {
        return req.orderItems().stream()
            .map(item -> new OrderItemBulkDto(
                item.itemName(),
                item.itemQuantity(),
                item.itemUnitValue(),
                item.itemValueCurrency(),
                item.itemWeight(),
                item.itemHSCode(),
                item.itemOriginCountryCode(),
                item.itemOriginStateCode(),
                item.itemOriginStateName(),
                item.weightUnit(),
                orderId,
                LocalDateTime.now(),
                LocalDateTime.now()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public OrderSenderBulkDto toSenderDto(OrderCreateRequest req, Long orderId, Long senderAddressId, Long sellerId) {
        return new OrderSenderBulkDto(
            orderId,
            sellerId,
            senderAddressId,
            req.senderCompany(),
            req.senderEmail(),
            req.senderName(),
            req.senderPhoneNo(),
            req.storeName(),
            req.storePlatform()
        );
    }

    @Override
    public OrderRecipientBulkDto toRecipientDto(OrderCreateRequest req, Long orderId, Long recipientAddressId) {
        return new OrderRecipientBulkDto(
            orderId,
            recipientAddressId,
            req.recipientCompany(),
            req.recipientEmail(),
            req.recipientName(),
            req.recipientPhoneNo()
        );
    }

    @Override
    public SenderAddressBulkDto toSenderAddressDto(OrderCreateRequest req, String masterNo) {
        return new SenderAddressBulkDto(
            masterNo,
            req.senderAddress1(),
            req.senderAddress2(),
            req.senderCity(),
            req.senderState(),
            req.senderStateCode() != null ? req.senderStateCode().name() : null,
            req.senderTaxId(),
            req.senderZipCode(),
            req.senderCountryCode().name()
        );
    }

    @Override
    public RecipientAddressBulkDto toRecipientAddressDto(OrderCreateRequest req, String masterNo) {
        return new RecipientAddressBulkDto(
            masterNo,
            req.recipientAddress1(),
            req.recipientAddress2(),
            req.recipientCity(),
            req.recipientState(),
            req.recipientStateCode() != null ? req.recipientStateCode().name() : null,
            req.recipientTaxId(),
            req.recipientZipCode(),
            req.recipientCountryCode().name()
        );
    }
}