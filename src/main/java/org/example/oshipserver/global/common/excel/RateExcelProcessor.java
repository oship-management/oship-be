package org.example.oshipserver.global.common.excel;

import java.math.BigDecimal;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RateExcelRequest;
import org.springframework.stereotype.Service;

@Service
public class RateExcelProcessor extends
    AbstractExcelUploadProcessor<RateExcelRequest, RateCreateRequest> {

    public RateExcelProcessor() {
        super(
            new ExcelParser<>(row -> new RateExcelRequest(
                (int) row.getCell(0).getNumericCellValue(),
                (long) row.getCell(1).getNumericCellValue(),
                (int) row.getCell(2).getNumericCellValue(),
                row.getCell(3).getNumericCellValue(),
                row.getCell(4).getNumericCellValue()
            )), 1);
    }

    @Override
    protected RateCreateRequest processRecord(RateExcelRequest dto) {
        return RateCreateRequest.builder()
            .carrierId(dto.carrierId())
            .zoneIndex(dto.zoneIndex())
            .amount(BigDecimal.valueOf(dto.weight()))
            .weight(BigDecimal.valueOf(dto.amount()))
            .build();
    }
}
