package org.example.oshipserver.domain.carrier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.admin.dto.request.RequestZone;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.entity.CarrierCountry;
import org.example.oshipserver.domain.carrier.repository.CarrierCountryRepository;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCarrierService {

    private final CarrierCountryRepository carrierCountryRepository;
    private final CarrierRepository carrierRepository;

    public void createZone(RequestZone dto) {

        Carrier carrier = carrierRepository.findById(dto.carrierId())
            .orElseThrow(
                () -> new ApiException("해당 id의 carrier가 없습니다.", ErrorType.INVALID_PARAMETER));

        CarrierCountry carrierCountry = CarrierCountry.builder()
            .carrier(carrier)
            .zoneNo(dto.zoneNo())
            .countryCode(dto.countryCode())
            .build();

        carrierCountryRepository.save(carrierCountry);
    }
}
