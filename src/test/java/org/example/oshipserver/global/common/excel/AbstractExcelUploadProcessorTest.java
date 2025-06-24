package org.example.oshipserver.global.common.excel;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult.ErrorDetail;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AbstractExcelUploadProcessorTest {

    @SuppressWarnings("unchecked")
    ExcelParser<String> parser = (ExcelParser<String>) mock(ExcelParser.class);
    private SkipValidationProcessor skipValidationProcessor;
    private ValidatingProcessor validatingProcessor;

    @BeforeEach
    void setUp() {
        parser = mock(ExcelParser.class);
        skipValidationProcessor = new SkipValidationProcessor(parser);
        validatingProcessor = new ValidatingProcessor(parser);
    }

    @Test
    @DisplayName("성공 레코드만 있는 경우, 모든 레코드가 성공 리스트에 포함된다.")
    void givenAllValidRecords_whenProcess_thenAllSuccess() throws Exception {
        // given
        var fakeResult = new ExcelParseResult<>(List.of("a", "b", "c"), List.of());
        given(parser.parse(any(InputStream.class))).willReturn(fakeResult);

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.xls",
            "application/vnd.ms-excel",
            "dummy".getBytes(StandardCharsets.UTF_8)
        );

        // when
        var result = skipValidationProcessor.process(file);

        // then
        then(result.success()).hasSize(3);
        then(result.errors()).isEmpty();
        then(result.success()).containsExactly(2, 2, 2);
        verify(parser, times(1)).parse(any());
    }

    @Test
    @DisplayName("일부 레코드 처리 중 예외 발생 시, 해당 인덱스가 errors 에 기록된다")
    void givenSomeRecordsThrow_whenProcess_thenErrorDetailCaptured() throws Exception {
        // given
        var fakeResult = new ExcelParseResult<>(
            List.of("ok", "fail", "ok2"),
            new ArrayList<>()
        );
        given(parser.parse(any())).willReturn(fakeResult);

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.xlsx",
            "application/vnd.ms-excel",
            "dummy".getBytes()
        );

        // when
        var result = skipValidationProcessor.process(file);

        // then
        then(result.success()).containsExactly(3, 4);
        then(result.errors()).hasSize(1);
        ErrorDetail err = result.errors().get(0);
        then(err.rowIndex()).isEqualTo(1);
        then(err.errorMessage()).contains("fail on fail");
        verify(parser, times(1)).parse(any());
    }

    @Test
    @DisplayName("잘못된 확장자 파일 업로드 시 INVALID_PARAMETER 예외")
    void givenWrongExtension_whenProcess_thenThrowInvalidParameter() {
        // --- Given ---
        validatingProcessor = new ValidatingProcessor(parser);
        MockMultipartFile file = new MockMultipartFile(
            "file", "not-excel.txt",
            "text/plain", "xxx".getBytes()
        );

        // --- When / Then ---
        var ex = assertThrows(ApiException.class, () -> validatingProcessor.process(file));
        then(ex.getErrorType()).isEqualTo(ErrorType.INVALID_PARAMETER);
        then(ex.getMessage()).contains("엑셀 파일");
    }

    public static class SkipValidationProcessor
    extends AbstractExcelUploadProcessor<String, Integer> {

        public SkipValidationProcessor(ExcelParser<String> parser) {
            super(parser, 1);
        }

        @Override
        protected void validateFile(MultipartFile file) {
            // 테스트에선 파일 검증을 통째로 skip
        }

        @Override
        protected Integer processRecord(String record) {
            // 예: "fail" 이 들어오면 예외, 아니면 길이+1 리턴
            if ("fail".equals(record)) {
                throw new IllegalArgumentException("fail on " + record);
            }
            return record.length() + 1;
        }
    }

    static class ValidatingProcessor extends AbstractExcelUploadProcessor<String, Integer> {
        public ValidatingProcessor(ExcelParser<String> parser) {
            super(parser, 1);
        }

        @Override
        protected Integer processRecord(String record) {
            return null;
        }
    }
}
