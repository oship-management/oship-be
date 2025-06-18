package org.example.oshipserver.global.common.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public class PoiExcelParser<T> implements ExcelParser<T> {

    private final Function<Row, T> mapper;

    public PoiExcelParser(Function<Row, T> mapper){
        this.mapper = mapper;
    }

    @Override
    public List<T> parse(InputStream in) {
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            return IntStream.rangeClosed(
                    sheet.getFirstRowNum() + 1, sheet.getLastRowNum()
                )
                .mapToObj(sheet::getRow)
                .filter(Objects::nonNull)
                .map(mapper)
                .toList();
        } catch (IOException e) {
            throw new ApiException("엑셀 파싱 실패", ErrorType.FAIL);
        }
    }
}
