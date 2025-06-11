package org.example.oshipserver.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LogInfo {

    private final String startTime;
    private final String method;
    private final String uri;
    private final String ip;
    private final String userAgent;
    private final long duration;

}

