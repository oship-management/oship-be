package org.example.oshipserver.domain.partner.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.oshipserver.domain.admin.dto.request.RateExcelRequest;
import org.example.oshipserver.global.common.excel.ExcelParser;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult.ErrorDetail;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RateExcelParser implements ExcelParser<RateExcelRequest> {

    @Override
    public ExcelParseResult<RateExcelRequest> parse(InputStream in) {
        List<RateExcelRequest> records = new ArrayList<>();
        List<ErrorDetail> errors = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            int firstRow = sheet.getFirstRowNum();
            int lastRow  = sheet.getLastRowNum();

            Row header = sheet.getRow(firstRow);
            int lastCol = header.getLastCellNum();
            List<String> zoneKey = IntStream.range(1, lastCol)
                .mapToObj(i -> header.getCell(i).getStringCellValue().trim())
                .map(raw -> raw.substring(raw.lastIndexOf('_') + 1))
                .toList();

            for (int i = firstRow+1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    double weight = row.getCell(0).getNumericCellValue();

                    Map<Integer, Double> amounts = new LinkedHashMap<>();
                    for(int j=0; j<zoneKey.size(); j++){
                        Row.MissingCellPolicy policy = MissingCellPolicy.RETURN_BLANK_AS_NULL;
                        var cell = row.getCell(j+1,policy);
                        double value = 0d;
                        if(cell != null) {
                            value = cell.getNumericCellValue();
                        }
                        amounts.put(Integer.valueOf(zoneKey.get(j)), value);
                    }

                    RateExcelRequest record = RateExcelRequest.builder()
                        .weight(weight)
                        .amounts(amounts)
                        .build();

                    records.add(record);
                } catch (Exception e) {
                    errors.add(new ErrorDetail(i, e.getMessage()));
                    log.warn("엑셀 파싱 실패 - {} 행: {}", i, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ApiException("엑셀 파싱 중 전체 실패", ErrorType.FAIL);
        }
        return new ExcelParseResult<>(records, errors);
    }
}


// 페덱스 따라서 예외처리하기
