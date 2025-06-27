package org.example.oshipserver.domain.admin.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RateGroupRequest;
import org.example.oshipserver.domain.admin.dto.request.RequestZone;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.carrier.service.AdminCarrierService;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;
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

    public ResponseRateDto uploadRateExcel(MultipartFile file, Long carrierId) {

        ExcelParseResult<RateCreateRequest> records = rateExcelProcessor.process(file);

        if (!records.errors().isEmpty()){
            return ResponseRateDto.from(records);
        }

        List<RateGroupRequest> grouped = records.success().stream()
            .flatMap(r -> r.amounts().entrySet().stream()
                .map(e -> Map.entry(
                    e.getKey(),
                    new RateGroupRequest.amounts(
                        r.weight(),
                        e.getValue()
                    )
                ))
            )
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ))
            .entrySet().stream()
            .map(e -> new RateGroupRequest(
                carrierId,
                e.getKey(),
                e.getValue()
            ))
            .toList();

        return adminCarrierService.createRate(grouped);
    }
}
