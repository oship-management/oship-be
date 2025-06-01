package org.example.oshipserver.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {

    private int status;
    private String message;
    private T data;

}

