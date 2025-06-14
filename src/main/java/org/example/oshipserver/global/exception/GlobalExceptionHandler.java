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
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
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
        log.error("error : " , e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseExceptionResponse(e.getStatusCode().value(), "API 오류"));

    }
    //500 에러 처리해야댐
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseExceptionResponse> handleError(RuntimeException e){
        log.error("error : ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseExceptionResponse(HttpStatus.BAD_REQUEST.value(), "API 오류"));

    }

}

