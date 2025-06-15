package org.example.oshipserver.domain.carrier.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.dto.PartnerCarrierNativeDto;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;

    /*
     * 참조하는 다른 entity
     * Order: getShipmentActualWeight, getShipmentVolumeWeight
     * RecipientAddress: recipientCountryCode
     *
     * 무게 측정
     * actualWeight(실제 무게), volumeWeight(부피 무게)를 비교
     * 더 큰 값을 기준으로 0.5kg 단위로 올림(20kg 미만인 경우)
     *
     * carriers를 가져오는 기준
     * 1. weight가 min~max 사이에 있는지
     * 2. 요금 만료일자가 지나지 않았는지
     * 3. 운송 가능한 곳인지
     *
     * 운송사별로 처리 가능한 order를 보여줌
     *
     */
    public List<PartnerCarrierNativeDto> getCarrierRatesForOrder(List<Long> orderIds) {
        List<PartnerCarrierNativeDto> responses = carrierRepository.findPartnerCarrierNative(
            orderIds);

        if (responses.isEmpty()) {
            throw new ApiException("운송 가능한 운송사가 없습니다.", ErrorType.INVALID_PARAMETER);
        }

        return responses;
    }
}
