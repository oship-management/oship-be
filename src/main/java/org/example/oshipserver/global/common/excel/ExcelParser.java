package org.example.oshipserver.global.common.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

@Slf4j
public class ExcelParser<T> {

    private final Function<Row, T> mapper;

    public ExcelParser(Function<Row, T> mapper){
        this.mapper = mapper;
    }

    public List<T> parse(InputStream in) {
        List<T> records = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            int first = sheet.getFirstRowNum() + 1;
            int last  = sheet.getLastRowNum();

            for (int i = first; i <= last; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    T dto = mapper.apply(row);
                    records.add(dto);
                } catch (Exception e) {
                    log.warn("엑셀 파싱 실패 - {} 행: {}", i, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ApiException("엑셀 파싱 중 전체 실패", ErrorType.FAIL);
        }
        return records;
    }
}
