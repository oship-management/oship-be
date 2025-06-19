package org.example.oshipserver.domain.admin.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;

@Builder
public record ResponseRateDto(
    int totalData,
    int totalError,
    List<ErrorDetail> errors
) {

    public static <T> ResponseRateDto from(ExcelParseResult<T> parseResult) {
        List<ErrorDetail> mappedErrors = parseResult.errors().stream()
            .map(e -> new ErrorDetail(e.rowIndex(), e.errorMessage()))
            .collect(Collectors.toList());

        int totalError = mappedErrors.size();
        int totalData = parseResult.success().size() + totalError;
        return new ResponseRateDto(totalData, totalError, mappedErrors);
    }

    public static record ErrorDetail(
        int rowIndex,
        String errorMessage) {
    }
}

