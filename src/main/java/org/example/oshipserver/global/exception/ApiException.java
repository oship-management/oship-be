package org.example.oshipserver.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException{

    private final String message;
    private final ErrorType errorType;

    // 외부 오류용(Toss): ErrorType 없이 message만 전달
    public ApiException(String message) {
        super(message);
        this.message = message;
        this.errorType = ErrorType.INTERNAL_SERVER_ERROR;
    }

    // 외부 오류용(Toss) : e.getResponseBodyAsString()과 함께 전달
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.errorType = ErrorType.INTERNAL_SERVER_ERROR;
    }
}
