package org.example.oshipserver.domain.order.dto.bulk;

import java.time.LocalDateTime;

public record OrderBulkDto(
    String orderNo,
    String oshipMasterNo,
    String shippingTerm,
    String serviceType,
    String weightUnit,
    Double shipmentActualWeight,
    Double shipmentVolumeWeight,
    Double dimensionWidth,
    Double dimensionLength,
    Double dimensionHeight,
    String packageType,
    Integer parcelCount,
    String itemContentsType,
    Boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    Long sellerId,
    String lastTrackingEvent
) {}