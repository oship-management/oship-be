package org.example.oshipserver.domain.order.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public class ExcelCellUtils {

    public static String getString(Row row, int i) {
        Cell cell = row.getCell(i);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue().trim();
        return value.isEmpty() ? null : value;
    }

    public static Double getDouble(Row row, int i) {
        try {
            String val = getString(row, i);
            return val == null ? null : Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new ApiException("숫자(Double) 형식이 잘못되었습니다. (열: " + (i + 1) + ")", ErrorType.VALID_FAIL);
        }
    }

    public static Integer getInt(Row row, int i) {
        try {
            String val = getString(row, i);
            return val == null ? null : (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new ApiException("숫자(Integer) 형식이 잘못되었습니다. (열: " + (i + 1) + ")", ErrorType.VALID_FAIL);
        }
    }
}
