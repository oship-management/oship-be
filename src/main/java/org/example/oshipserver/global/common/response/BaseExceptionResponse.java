package org.example.oshipserver.global.common.response;

public record BaseExceptionResponse(
        int status,
        String message
) {}
