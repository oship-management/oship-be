package org.example.oshipserver.global.common.excel.record;

import java.util.List;

public record ExcelParseResult<T> (
    List<T> success,
    List<ErrorDetail> errors
){
    public static record ErrorDetail(
        int rowIndex,
        String errorMessage
    ){}
}
