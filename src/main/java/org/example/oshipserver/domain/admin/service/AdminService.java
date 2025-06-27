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
import org.example.oshipserver.global.common.excel.RateExcelProcessor;
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

    public ResponseRateDto uploadRateExcel(MultipartFile file){
        ExcelParseResult<RateCreateRequest> records = rateExcelProcessor.process(file);

        if (!records.errors().isEmpty()){
            return ResponseRateDto.from(records);
        }

        List<RateGroupRequest> results =
            records.success().stream()
                .collect(Collectors.groupingBy(r ->
                    Map.entry(r.carrierId(), r.zoneIndex())
                ))
                .entrySet().stream()
                .map(e -> {
                    Long carrierId = e.getKey().getKey();
                    Integer zoneIndex = e.getKey().getValue();
                    List<RateGroupRequest.amounts> amounts = e.getValue().stream()
                        .map(r -> new RateGroupRequest.amounts(r.weight(), r.amount()))
                        .collect(Collectors.toList());
                    return new RateGroupRequest(carrierId, zoneIndex, amounts);
                })
                .toList();

        return adminCarrierService.createRate(results);
    }
}
