package org.example.oshipserver.domain.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.OrderRateResponseDto;
import org.example.oshipserver.domain.order.entity.OrderRecipient;
import org.example.oshipserver.domain.order.repository.OrderRecipientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderRateService {

    private final OrderRecipientRepository orderRecipientRepository;

    @Transactional(readOnly = true)
    public List<OrderRateResponseDto> getOrderInfos(List<Long> orderIds) {

        List<OrderRecipient> recipients = orderRecipientRepository.findByOrderIdIn(orderIds);

        return recipients.stream()
            .map(recipient -> OrderRateResponseDto.builder()
                .orderId(recipient.getOrder().getId())
                .weight(calculateWeight(recipient.getOrder().getShipmentActualWeight(), recipient.getOrder().getShipmentVolumeWeight()))
                .countryCode(recipient.getRecipientAddress().getRecipientCountryCode())
                .build()
            )
            .toList();
    }

    private BigDecimal calculateWeight(BigDecimal actualWeight, BigDecimal volumeWeight) {

        BigDecimal finalWeight = actualWeight.max(volumeWeight);

        if (finalWeight.compareTo(BigDecimal.valueOf(20)) < 0) {
            BigDecimal doubled = finalWeight.multiply(BigDecimal.valueOf(2));
            BigDecimal ceiled = doubled.setScale(0, RoundingMode.CEILING);
            finalWeight = ceiled.divide(BigDecimal.valueOf(2), 1, RoundingMode.UNNECESSARY);
        }

        return finalWeight;
    }

}
