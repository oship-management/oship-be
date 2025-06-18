package org.example.oshipserver.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.example.oshipserver.domain.order.dto.OrderItemDto;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.entity.enums.StateCode;
import org.example.oshipserver.domain.order.service.validation.annotation.ValidStateCode;

@ValidStateCode
public record OrderCreateRequest(

    // 주문 정보
    @NotBlank String storePlatform,
    @NotBlank String orderNo,
    @NotBlank String storeName,

    // 발송자 정보
    @NotBlank String senderName,
    String senderCompany,
    @Email @NotBlank String senderEmail,
    @NotBlank String senderPhoneNo,
    @NotNull CountryCode senderCountryCode,
    @NotBlank String senderState,
    StateCode senderStateCode,
    @NotBlank String senderCity,
    @NotBlank String senderAddress1,
    String senderAddress2,
    @NotBlank String senderZipCode,
    String senderTaxId,

    // 수취인 정보
    @NotBlank String recipientName,
    String recipientCompany,
    @Email @NotBlank String recipientEmail,
    @NotBlank String recipientPhoneNo,
    @NotNull  CountryCode recipientCountryCode,
    @NotBlank String recipientState,
    StateCode recipientStateCode,
    @NotBlank String recipientCity,
    @NotBlank String recipientAddress1,
    String recipientAddress2,
    @NotBlank String recipientZipCode,
    String recipientTaxId,

    // 배송 정보
    @NotBlank String itemContentsType,
    @Min(1) int parcelCount,
    @NotBlank String serviceType,
    @DecimalMin("0.0") double shipmentActualWeight,
    @DecimalMin("0.0") double shipmentVolumeWeight,
    @NotBlank String weightUnit,
    @Min(1) int dimensionWidth,
    @Min(1) int dimensionLength,
    @Min(1) int dimensionHeight,
    @NotBlank String packageType,
    @NotBlank String shippingTerm,
    @NotNull Long sellerId,

    @NotEmpty List<@Valid OrderItemDto> orderItems
) {}