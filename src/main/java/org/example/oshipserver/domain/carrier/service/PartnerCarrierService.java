package org.example.oshipserver.domain.carrier.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.admin.dto.request.RateGroupRequest;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto.ErrorDetail;
import org.example.oshipserver.domain.carrier.entity.Carrier;
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
public class PartnerCarrierService {

    private final CarrierRateChargeRepository carrierRateChargeRepository;
    private final CarrierCountryRepository carrierCountryRepository;
    private final CarrierRepository carrierRepository;

    @Transactional
    public ResponseRateDto createRate(List<RateGroupRequest> records) {

        int failCount = 0;
        int totalCount = 0;
        List<ResponseRateDto.ErrorDetail> errors = new ArrayList<>();
        List<CarrierRateCharge> rateCharges = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            RateGroupRequest request = records.get(i);
            try {
                Carrier carrier = carrierRepository.findById(request.carrierId())
                    .orElseThrow(() -> new ApiException("해당 id의 carrier가 없습니다.",
                        ErrorType.INVALID_PARAMETER));

                carrierRateChargeRepository.softDeleteByCarrierIdAndZoneAndDeletedAtIsNull(
                    request.carrierId(),
                    request.zoneIndex());

                for (RateGroupRequest.amounts amount : request.amounts()) {
                    rateCharges.add(CarrierRateCharge.builder()
                        .carrier(carrier)
                        .zoneIndex(request.zoneIndex())
                        .weight(amount.weight())
                        .amount(amount.amount())
                        .build());
                    totalCount++;
                }

            } catch (Exception ex) {
                failCount++;
                errors.add(new ErrorDetail(i, ex.getMessage()));
                log.warn("Rate 생성 건너뛰기: dto={}, error={}", request, ex.getMessage());
            }
        }
        carrierRateChargeRepository.saveAll(rateCharges);

        return ResponseRateDto.builder()
            .totalData(totalCount)
            .totalError(failCount)
            .errors(errors)
            .build();
    }

    @Transactional(readOnly = true)
    public boolean validateZone(int zoneNo, Long carrierId) {

        return carrierCountryRepository.existsByZoneNoAndCarrierId(zoneNo, carrierId);
    }

    @Transactional(readOnly = true)
    public void findCarrierOrThrow(Long partnerId, Long carrierId) {
        carrierRepository.findCarrierByIdAndPartnerId(partnerId, carrierId)
            .orElseThrow(() -> new ApiException("partner에 등록되지 않은 carrierId입니다.", ErrorType.INVALID_PARAMETER));
    }
}
