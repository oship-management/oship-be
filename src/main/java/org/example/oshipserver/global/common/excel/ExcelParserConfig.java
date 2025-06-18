package org.example.oshipserver.global.common.excel;

import org.example.oshipserver.domain.admin.dto.request.RateExcelRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelParserConfig {

    @Bean
    public ExcelParser<RateExcelRequest> rateParser() {
        return new PoiExcelParser<>(row -> {
            return new RateExcelRequest(
                (long) row.getCell(0).getNumericCellValue(),
                (int) row.getCell(1).getNumericCellValue(),
                row.getCell(2).getNumericCellValue(),
                row.getCell(3).getNumericCellValue()
            );
        });
    }
}
