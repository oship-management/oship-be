package org.example.oshipserver.domain.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.admin.dto.RequestZone;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.carrier.service.AdminCarrierService;
import org.example.oshipserver.global.common.excel.RateExcelProcessor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminCarrierService adminCarrierService;
    private final RateExcelProcessor rateExcelProcessor;

    public void createZone(RequestZone dto){
        adminCarrierService.createZone(dto);
    }

    public ResponseRateDto uploadRateExcel(MultipartFile file){
        List<RateCreateRequest> dtos = rateExcelProcessor.process(file);

        return adminCarrierService.createRate(dtos);
    }
}
