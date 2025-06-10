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

    // Payment 관련 에러
    DUPLICATED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "지원하지 않는 결제 방식입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 주문 ID 형식입니다.");

    // Shipping/Barcode 관련 에러
    BARCODE_NOT_PRINTED(HttpStatus.BAD_REQUEST, "바코드가 출력되지 않았습니다."),
    AWB_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 AWB가 발행된 주문입니다."),
    CARRIER_MISMATCH(HttpStatus.BAD_REQUEST, "배송업체가 일치하지 않습니다."),
    INVALID_BOX_NUMBER(HttpStatus.BAD_REQUEST, "유효하지 않은 박스 번호입니다."),
    INVALID_BARCODE_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 바코드 형식입니다.");

    private final HttpStatus status;
    private final String desc;
}

