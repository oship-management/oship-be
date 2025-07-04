package org.example.oshipserver.domain.partner.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RateExcelRequest;
import org.example.oshipserver.global.common.excel.AbstractExcelUploadProcessor;
import org.springframework.stereotype.Service;

@Service
public class RateExcelProcessor extends
    AbstractExcelUploadProcessor<RateExcelRequest, RateCreateRequest> {

    public RateExcelProcessor(RateExcelParser parser) {
        super(
            parser, 1);
    }

    @Override
    protected RateCreateRequest processRecord(RateExcelRequest dto) {
        Map<Integer, BigDecimal> bigAmounts = dto.amounts().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> BigDecimal.valueOf(e.getValue())
            ));

        return RateCreateRequest.builder()
            .weight(BigDecimal.valueOf(dto.weight()))
            .amounts(bigAmounts)
            .build();
    }
}
