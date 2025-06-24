package org.example.oshipserver.domain.order.dto.bulk;

public record OrderRecipientBulkDto(
    Long orderId,                  // FK: orders.id
    Long recipientAddressId,       // FK: recipient_addresses.id
    String recipientCompany,
    String recipientEmail,
    String recipientName,
    String recipientPhoneNo
) {}
