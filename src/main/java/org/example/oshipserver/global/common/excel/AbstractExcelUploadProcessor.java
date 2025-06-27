package org.example.oshipserver.global.common.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult.ErrorDetail;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public abstract class AbstractExcelUploadProcessor<T, R> {

    private final ExcelParser<T> parser;
    private final long fileSize;

    protected AbstractExcelUploadProcessor(ExcelParser<T> parser, int fileSize) {
        this.parser = parser;
        this.fileSize = (long) fileSize * 1024 * 1024;
    }

    public ExcelParseResult<R> process(MultipartFile file) {
        validateFile(file);

        ExcelParseResult<T> records = parseWorkbook(file);

        List<R> results = new ArrayList<>(records.success().size());
        for (int i = 0; i < records.success().size(); i++) {
            T record = records.success().get(i);
            try {
                R item = processRecord(record);
                results.add(item);
            } catch (Exception e) {
                records.errors().add(new ErrorDetail(i, e.getMessage()));
                handleRecordError(i, record, e);
            }
        }

        return new ExcelParseResult<>(results, records.errors());
    }

    protected void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new ApiException("파일 이름이 없습니다.", ErrorType.INVALID_PARAMETER);
        }
        String ext = FilenameUtils.getExtension(filename)
            .toLowerCase(Locale.ROOT);

        if (!"xls".equals(ext) && !"xlsx".equals(ext)) {
            throw new ApiException("엑셀 파일(.xls 또는 .xlsx)만 업로드 가능합니다.", ErrorType.INVALID_PARAMETER);
        }

        if (file.getSize() > fileSize) {
            throw new ApiException(fileSize + "MB 이하 파일만 업로드 할 수 있습니다.",
                ErrorType.INVALID_PARAMETER);
        }

        try (InputStream in = file.getInputStream()) {
            WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new ApiException("유효한 엑셀 파일이 아닙니다.", ErrorType.INVALID_PARAMETER);
        }
    }

    private ExcelParseResult<T> parseWorkbook(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return parser.parse(in);
        } catch (Exception e) {
            throw new ApiException("엑셀 파싱 중 오류", ErrorType.FAIL);
        }
    }

    private void handleRecordError(int index, T record, Exception ex) {
        log.warn("처리 실패 at index={}, record={} error={}", index, record, ex.getMessage());
    }

    protected abstract R processRecord(T record);
}
