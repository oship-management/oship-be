package org.example.oshipserver.domain.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorType implements ErrorCode {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    UNSUPPORTED_AUTH_METHOD(HttpStatus.BAD_REQUEST, "지원하지 않는 인증 방식입니다."),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "잘못된 사용자 역할입니다.");

    private final HttpStatus status;
private final String desc;

}
