package org.example.oshipserver.domain.order.service;

import org.example.oshipserver.domain.order.dto.OrderInfoDto;

public interface OrderInfoFacade {

    /**
     * orderId -> shipmentActualWeight, shipmentVolumeWeight, countryCode 조회
     */
    OrderInfoDto getOrderInfo(Long orderId);
}
