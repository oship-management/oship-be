package org.example.oshipserver.domain.order.dto.bulk;

public record OrderSenderBulkDto(
    Long orderId,              // FK: orders.id
    Long sellerId,             // Nullable
    Long senderAddressId,      // FK: sender_addresses.id
    String senderCompany,
    String senderEmail,
    String senderName,
    String senderPhoneNo,
    String storeName,
    String storePlatform
) {}
