package org.example.oshipserver.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException{

    private final String message;
    private final ErrorCode errorType;
}
