package org.example.oshipserver.domain.carrier.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RequestZone;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto.ErrorDetail;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.entity.CarrierCountry;
import org.example.oshipserver.domain.carrier.entity.CarrierRateCharge;
import org.example.oshipserver.domain.carrier.repository.CarrierCountryRepository;
import org.example.oshipserver.domain.carrier.repository.CarrierRateChargeRepository;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCarrierService {

    private final CarrierCountryRepository carrierCountryRepository;
    private final CarrierRateChargeRepository carrierRateChargeRepository;
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

    @Transactional
    public ResponseRateDto createRate(List<RateCreateRequest> records) {

        int failCount = 0;
        List<CarrierRateCharge> rateCharges = new ArrayList<>();
        List<ResponseRateDto.ErrorDetail> errors = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            RateCreateRequest request = records.get(i);
            try {
                Carrier carrier = carrierRepository.findById(request.carrierId())
                    .orElseThrow(() -> new ApiException("해당 id의 carrier가 없습니다.",
                        ErrorType.INVALID_PARAMETER));

                rateCharges.add(CarrierRateCharge.builder()
                    .carrier(carrier)
                    .zoneIndex(request.zoneIndex())
                    .weight(request.weight())
                    .amount(request.amount())
                    .build());
            } catch (Exception ex) {
                failCount++;
                errors.add(new ErrorDetail(i, ex.getMessage()));
                log.warn("Rate 생성 건너뛰기: dto={}, error={}", request, ex.getMessage());
            }
        }
        carrierRateChargeRepository.saveAll(rateCharges);

        return ResponseRateDto.builder()
            .totalData(records.size())
            .totalError(failCount)
            .errors(errors)
            .build();
    }
}
