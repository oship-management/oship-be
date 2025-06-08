package org.example.oshipserver.domain.order.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.oshipserver.domain.order.dto.request.OrderExcelRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
                    // 각 셀을 읽어 DTO 생성
                    OrderExcelRequest dto = new OrderExcelRequest(
                        getString(row, 0),   // storePlatform
                        getString(row, 1),   // storeName
                        getString(row, 2),   // orderNo
                        getString(row, 3),   // sellerId
                        getString(row, 4),   // shippingTerm
                        getString(row, 5),   // senderName
                        getString(row, 6),   // senderCompany
                        getString(row, 7),   // senderEmail
                        getString(row, 8),   // senderPhoneNo
                        getString(row, 9),   // senderAddress1
                        getString(row, 10),  // senderAddress2
                        getString(row, 11),  // senderCity
                        getString(row, 12),  // senderState
                        getString(row, 13),  // senderZipCode
                        getString(row, 14),  // senderCountryCode
                        getString(row, 15),  // senderTaxId
                        getString(row, 16),  // recipientName
                        getString(row, 17),  // recipientCompany
                        getString(row, 18),  // recipientEmail
                        getString(row, 19),  // recipientPhoneNo
                        getString(row, 20),  // recipientTaxId
                        getString(row, 21),  // recipientAddress1
                        getString(row, 22),  // recipientAddress2
                        getString(row, 23),  // recipientCity
                        getString(row, 24),  // recipientState
                        getString(row, 25),  // recipientZipCode
                        getString(row, 26),  // recipientCountryCode
                        getDouble(row, 27),  // shipmentActualWeight
                        getDouble(row, 28),  // shipmentVolumeWeight
                        getString(row, 29),  // weightUnit
                        getDouble(row, 30),  // dimensionWidth
                        getDouble(row, 31),  // dimensionLength
                        getDouble(row, 32),  // dimensionHeight
                        getString(row, 33),  // packageType
                        getInt(row, 34),     // parcelCount
                        getString(row, 35),  // serviceType
                        getString(row, 36),  // itemContentsType
                        getString(row, 37),  // itemName
                        getInt(row, 38),     // itemQuantity
                        getDouble(row, 39),  // itemUnitValue
                        getString(row, 40),  // itemValueCurrency
                        getDouble(row, 41),  // itemWeight
                        getString(row, 42),  // itemHSCode
                        getString(row, 43)   // itemOriginCountryCode
                    );
                    result.add(dto);

                } catch (Exception e) {
                    // 한 줄 파싱 실패 → 행 번호 포함하여 상세 예외 전달
                    throw new IllegalArgumentException("엑셀 파싱 중 오류 (row " + (i + 1) + "): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // 파일 열기 실패 또는 전체 실패 시
            throw new IllegalArgumentException("엑셀 파일 처리 실패: " + e.getMessage());
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

    /**
     * 셀에서 문자열 값 추출 (null-safe)
     *
     * @param row 대상 Row
     * @param i   셀 인덱스
     * @return 문자열 값 또는 null
     */
    private String getString(Row row, int i) {
        Cell cell = row.getCell(i);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue().trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * 셀에서 Double 값 추출 (문자열 → Double), 실패 시 예외 발생
     *
     * @param row 대상 Row
     * @param i   셀 인덱스
     * @return Double 값 또는 null
     */
    private Double getDouble(Row row, int i) {
        try {
            String val = getString(row, i);
            return val == null ? null : Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자(Double) 형식이 잘못되었습니다. (열: " + (i + 1) + ")");
        }
    }

    /**
     * 셀에서 Integer 값 추출 (문자열 → Double → int), 실패 시 예외 발생
     *
     * @param row 대상 Row
     * @param i   셀 인덱스
     * @return Integer 값 또는 null
     */
    private Integer getInt(Row row, int i) {
        try {
            String val = getString(row, i);
            return val == null ? null : (int) Double.parseDouble(val); // Excel은 정수도 Double로 읽는 경우 많음
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자(Integer) 형식이 잘못되었습니다. (열: " + (i + 1) + ")");
        }
    }
}
