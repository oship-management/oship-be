package org.example.oshipserver.domain.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessType{

    OK(HttpStatus.OK.value(), "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED.value(), "리소스가 성공적으로 생성되었습니다."),
    UPDATED(HttpStatus.OK.value(), "리소스가 성공적으로 수정되었습니다."),
    DELETED(HttpStatus.OK.value(), "리소스가 성공적으로 삭제되었습니다."),
    LOGGED_IN(HttpStatus.OK.value(), "로그인이 성공적으로 처리되었습니다."),
    SIGNED_UP(HttpStatus.CREATED.value(), "회원가입이 성공적으로 처리되었습니다.");

private final int status;
private final String message;
}
