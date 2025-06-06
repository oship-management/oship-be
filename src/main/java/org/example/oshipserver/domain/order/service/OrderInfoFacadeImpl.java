package org.example.oshipserver.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.OrderInfoDto;
import org.example.oshipserver.domain.order.entity.OrderRecipient;
import org.example.oshipserver.domain.order.repository.OrderRecipientRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderInfoFacadeImpl implements OrderInfoFacade{

    private final OrderRecipientRepository orderRecipientRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderInfoDto getOrderInfo(Long orderId){

        OrderRecipient findOrderRecipient = orderRecipientRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ApiException("해당 orderID의 orderRecipient가 없습니다.", ErrorType.INVALID_PARAMETER));

        return OrderInfoDto.builder()
            .shipmentActualWeight(findOrderRecipient.getOrder().getShipmentActualWeight())
            .shipmentVolumeWeight(findOrderRecipient.getOrder().getShipmentVolumeWeight())
            .countryCode(findOrderRecipient.getRecipientAddress().getRecipientCountryCode())
            .build();
    }

}
