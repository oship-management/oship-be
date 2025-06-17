package org.example.oshipserver.domain.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.aspectj.util.FileUtil;
import org.example.oshipserver.domain.admin.dto.RequestZone;
import org.example.oshipserver.domain.carrier.service.AdminCarrierService;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminCarrierService adminCarrierService;
    private static final int EXCEL_UPLOAD_THREAD_POOL_SIZE = 10; // 주문 처리용 스레드 풀 개수
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    public void createZone(RequestZone dto){
        adminCarrierService.createZone(dto);
    }

    public void uploadRateExcel(MultipartFile file){
        String ext = FilenameUtils.getExtension(file.getOriginalFilename())
            .toLowerCase();

        if (!"xls".equals(ext) && !"xlsx".equals(ext)) {
            throw new ApiException("엑셀 파일(.xls 또는 .xlsx)만 업로드 가능합니다.", ErrorType.INVALID_PARAMETER);
        }

        if(file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException("10MB 이하 파일만 업로드 할 수 있습니다.", ErrorType.INVALID_PARAMETER);
        }

        try (InputStream in = file.getInputStream()) {
            WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new ApiException("유효한 엑셀 파일이 아닙니다.", ErrorType.INVALID_PARAMETER);
        }

        try (InputStream in = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트
            // 헤더 행을 건너뛰고 1부터 시작
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 셀에서 값 읽기 (예시)
                    Long carrierId      = (long) row.getCell(0).getNumericCellValue();
                    Integer zoneIndex   = (int)  row.getCell(1).getNumericCellValue();
                    BigDecimal weight   = BigDecimal.valueOf(row.getCell(2).getNumericCellValue());
                    BigDecimal amount   = BigDecimal.valueOf(row.getCell(3).getNumericCellValue());

                    // DTO 생성
                    RequestZone dto = new RequestZone(carrierId, zoneIndex, List.of(
                        new RequestZone.WeightAmount(weight, amount)
                    ));

                    // 서비스 호출
                    adminCarrierService.createRate(dto);

                } catch (Exception ex) {
                    // 이 행만 건너뛰고 싶으면 예외를 잡아서 로깅
                    log.warn("엑셀 {}행 처리 중 실패: {}", i, ex.getMessage());
                }
            }
        } catch (IOException ioe) {
            throw new ApiException("파일 처리 중 오류가 발생했습니다.", ErrorType.INVALID_PARAMETER);
        }


    }
}
