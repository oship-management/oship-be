package org.example.oshipserver.domain.order.dto.bulk;

public record RecipientAddressBulkDto(
    String oshipMasterNo,               // 임시 연결용 키 (order, address 매핑용)
    String recipientAddress1,
    String recipientAddress2,
    String recipientCity,
    String recipientState,
    String recipientStateCode,
    String recipientTaxId,
    String recipientZipCode,
    String recipientCountryCode
) {}
