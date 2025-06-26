package org.example.oshipserver.global.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.global.common.response.BaseExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        // FieldError 처리 (기존 필드 단위 @NotNull 등)
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        // ObjectError 처리 (같은 클래스 수준 검증)
        e.getBindingResult().getGlobalErrors().forEach(error -> {
            String objectName = error.getObjectName(); // ex: orderCreateRequest
            errors.put(objectName, error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseExceptionResponse> handleApiException(ApiException e) {
        log.error("ApiException occurred. type={} message={} classNam={}", e.getErrorType()
        , e.getMessage(), e.getClass().getName());
        return ResponseEntity
                .status(e.getErrorType().getStatus())
                .body(new BaseExceptionResponse(
                        e.getErrorType().getStatus().value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseExceptionResponse> handleAuthenticationException(AuthenticationException ex) {

        return ResponseEntity
                .status(ErrorType.UNAUTHORIZED.getStatus())
                .body(new BaseExceptionResponse(
                        ErrorType.UNAUTHORIZED.getStatus().value(),
                        ex.getMessage()
                ));
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseExceptionResponse> handleAuthenticationException(AccessDeniedException ex) {

        return ResponseEntity
                .status(ErrorType.FORBIDDEN.getStatus())
                .body(new BaseExceptionResponse(
                        ErrorType.FORBIDDEN.getStatus().value(),
                        ex.getMessage()
                ));
    }

    //외부클라이언트에러
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<BaseExceptionResponse> handleHttpClient(HttpClientErrorException e){
        log.error("서버 내부 HttpClientException 발생: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseExceptionResponse(e.getStatusCode().value(), e.getMessage()));

    }
    //500 에러 처리해야댐
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseExceptionResponse> handleRuntimeException(RuntimeException e) {
        // 스택 트레이스 + 원인까지 출력
        log.error("서버 내부 RuntimeException 발생: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류 발생"));
    }

}

