package org.example.oshipserver.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorType{
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 파라미터 요청입니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출 에러입니다."),
    FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리에 실패했습니다."),
    DB_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "요청 DB 처리가 실패하였습니다."),
    VALID_FAIL(HttpStatus.BAD_REQUEST, "유효성 검증에 실패하였습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT FOUND"),

    // Order 관련 에러
    DUPLICATED_ORDER(HttpStatus.BAD_REQUEST, "이미 동일한 주문번호가 존재합니다"),

    //인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "잘못된 형식의 토큰입니다."),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "토큰의 서명이 유효하지 않습니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    TOKEN_ILLEGAL_ARGUMENT(HttpStatus.UNAUTHORIZED, "토큰 값이 비어있거나 잘못되었습니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),

    // Payment 관련 에러
    DUPLICATED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "지원하지 않는 결제 방식입니다."),
    ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 전체 금액이 취소되었습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다."),
    INVALID_ORDER(HttpStatus.BAD_REQUEST, "해당 주문은 이 결제에 포함되어 있지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    PAYMENT_STATUS_TRANSITION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 상태 전이에 실패했습니다."),
    PAYMENT_INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "부분취소 금액이 유효하지 않습니다."),

    // TOSS API 호출 에러
    REDIS_RETRY_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "재시도 중 장애 발생, 나중에 다시 시도해주세요"),
    TOSS_PAYMENT_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "결제 요청 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),

    // Shipping/Barcode 관련 에러
    BARCODE_NOT_PRINTED(HttpStatus.BAD_REQUEST, "바코드가 출력되지 않았습니다."),
    AWB_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 AWB가 발행된 주문입니다."),
    CARRIER_MISMATCH(HttpStatus.BAD_REQUEST, "배송업체가 일치하지 않습니다."),
    INVALID_BOX_NUMBER(HttpStatus.BAD_REQUEST, "유효하지 않은 박스 번호입니다."),
    INVALID_BARCODE_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 바코드 형식입니다."),

    // FEDEX API 호출 에러
    EXTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 인증에 실패하였습니다."),
    FEDEX_BAD_REQUEST(HttpStatus.BAD_REQUEST, "FEDEX API 스펙에 맞지 않는 값이 존재합니다");

    private final HttpStatus status;
    private final String desc;

    public String getMessage() {
        return desc;
    }

}

