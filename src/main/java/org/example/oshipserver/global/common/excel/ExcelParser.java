package org.example.oshipserver.global.common.excel;

import java.io.InputStream;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;

public interface ExcelParser<T> {

    public ExcelParseResult<T> parse(InputStream in);
}
