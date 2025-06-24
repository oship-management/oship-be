package org.example.oshipserver.domain.order.dto.bulk;

public record SenderAddressBulkDto(
    String oshipMasterNo,          // 임시 키, orderId 매핑 전 기준
    String senderAddress1,
    String senderAddress2,
    String senderCity,
    String senderState,
    String senderStateCode,
    String senderTaxId,
    String senderZipCode,
    String senderCountryCode
) {}
