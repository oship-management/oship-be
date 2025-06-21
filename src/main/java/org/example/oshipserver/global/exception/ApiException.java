package org.example.oshipserver.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
public class ApiException extends RuntimeException{

    private final ErrorType errorType;

    /**
     * ErrorType만 전달할 경우 (errorType의 메시지를 사용)
     */
    public ApiException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    /**
     * 외부 API 오류 등으로 message만 따로 지정
     * ErrorType : 기본값인 INTERNAL_SERVER_ERROR
     */
    public ApiException(String message) {
        super(message);
        this.errorType = ErrorType.INTERNAL_SERVER_ERROR;
    }

    /**
     * 원인 예외 포함 + message 지정 시 사용
     * ErrorType : 기본값인 INTERNAL_SERVER_ERROR
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.INTERNAL_SERVER_ERROR;
    }

    /**
     * message + ErrorType 모두 직접 지정
     */
    public ApiException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}
