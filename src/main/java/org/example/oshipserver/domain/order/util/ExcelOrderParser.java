package org.example.oshipserver.domain.order.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.oshipserver.domain.order.dto.request.OrderExcelRequest;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelOrderParser {

    /**
     * 엑셀 파일을 읽어 OrderExcelRequest 리스트로 변환
     *
     * @param file 업로드된 Excel 파일 (.xlsx 형식)
     * @return OrderExcelRequest 리스트
     * @throws IllegalArgumentException 엑셀 파싱 실패 시 행 번호와 함께 메시지 포함
     */
    public List<OrderExcelRequest> parse(MultipartFile file) {
        List<OrderExcelRequest> result = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 사용
            int startRow = 1; // 헤더 이후부터 읽기 시작

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;

                try {
                    OrderExcelRequest dto = OrderExcelRequest.from(row);
                    result.add(dto);
                } catch (Exception e) {
                    throw new ApiException("엑셀 파싱 중 오류 (row " + (i + 1) + "): " + e.getMessage(), ErrorType.VALID_FAIL);
                }
            }
        } catch (Exception e) {
            throw new ApiException("엑셀 파일 처리 실패: " + e.getMessage(), ErrorType.FAIL);
        }

        return result;
    }

    /**
     * 주어진 Row가 모든 셀이 비어있는지 여부 확인
     *
     * @param row 엑셀 Row
     * @return true면 빈 행
     */
    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
