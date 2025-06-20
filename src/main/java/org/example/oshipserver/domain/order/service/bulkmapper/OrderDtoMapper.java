package org.example.oshipserver.domain.order.service.bulkmapper;

import java.util.List;
import org.example.oshipserver.domain.order.dto.bulk.*;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;

public interface OrderDtoMapper {
    OrderBulkDto toOrderDto(OrderCreateRequest req, String masterNo, Long sellerId);
    List<OrderItemBulkDto> toOrderItemDtos(OrderCreateRequest req, String masterNo, Long orderId);
    OrderSenderBulkDto toSenderDto(OrderCreateRequest req, Long orderId, Long senderAddressId, Long sellerId);
    OrderRecipientBulkDto toRecipientDto(OrderCreateRequest req, Long orderId, Long recipientAddressId);
    SenderAddressBulkDto toSenderAddressDto(OrderCreateRequest req, String masterNo);
    RecipientAddressBulkDto toRecipientAddressDto(OrderCreateRequest req, String masterNo);
}
