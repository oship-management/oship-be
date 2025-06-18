package org.example.oshipserver.global.common.excel;

import java.math.BigDecimal;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RateExcelRequest;
import org.springframework.stereotype.Service;

@Service
public class RateExcelProcessor extends
    AbstractExcelUploadProcessor<RateExcelRequest, RateCreateRequest> {

    public RateExcelProcessor(ExcelParser<RateExcelRequest> parser) {
        super(parser, 5);
    }

    @Override
    protected RateCreateRequest processRecord(RateExcelRequest dto){
        return RateCreateRequest.builder()
            .carrierId(dto.carrierId())
            .zoneIndex(dto.zoneIndex())
            .amount(BigDecimal.valueOf(dto.weight()))
            .weight(BigDecimal.valueOf(dto.amount()))
            .build();
    }

}
