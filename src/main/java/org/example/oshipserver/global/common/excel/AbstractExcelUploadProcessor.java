package org.example.oshipserver.global.common.excel;

import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public abstract class AbstractExcelUploadProcessor<T, R> {

    private final ExcelParser<T> parser;
    private final ExecutorService executor;
    private final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    protected AbstractExcelUploadProcessor(ExcelParser<T> parser, int threadPoolSize) {
        this.parser = parser;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public List<R> process(MultipartFile file) {
        validateFile(file);

        List<T> dtos = parseWorkbook(file);

        List<CompletableFuture<R>> futures = dtos.stream()
            .map(dto -> CompletableFuture.supplyAsync(() -> {
                try {
                    return processRecord(dto);
                } catch (Exception ex) {
                    handleRecordError(dto, ex);
                    return null;
                }
            }, executor))
            .toList();

        return futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .toList();
    }

    private void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new ApiException("파일 이름이 없습니다.", ErrorType.INVALID_PARAMETER);
        }
        String ext = FilenameUtils.getExtension(filename)
            .toLowerCase(Locale.ROOT);

        if (!"xls".equals(ext) && !"xlsx".equals(ext)) {
            throw new ApiException("엑셀 파일(.xls 또는 .xlsx)만 업로드 가능합니다.", ErrorType.INVALID_PARAMETER);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException("10MB 이하 파일만 업로드 할 수 있습니다.", ErrorType.INVALID_PARAMETER);
        }

        try (InputStream in = file.getInputStream()) {
            WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new ApiException("유효한 엑셀 파일이 아닙니다.", ErrorType.INVALID_PARAMETER);
        }
    }

    private List<T> parseWorkbook(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return parser.parse(in);
        } catch (Exception e) {
            throw new ApiException("엑셀 파싱 중 오류", ErrorType.FAIL);
        }
    }

    protected void handleRecordError(T dto, Exception ex) {
        log.warn("처리 실패, dto={}", dto, ex);
    }

    protected abstract R processRecord(T dto);

    @PreDestroy
    public void destroy() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
