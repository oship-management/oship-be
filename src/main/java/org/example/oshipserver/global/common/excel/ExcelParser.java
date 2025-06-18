package org.example.oshipserver.global.common.excel;

import java.io.InputStream;
import java.util.List;

public interface ExcelParser<T> {

    List<T> parse(InputStream in);
}
