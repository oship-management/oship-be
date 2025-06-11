package org.example.oshipserver.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@AllArgsConstructor
public class LogInfo {

    private final String date;
    private Long userId;
    private final String method;
    private final String uri;
    private final String ip;
    private final String userAgent;
    @Setter
    private long duration;
    @Setter
    private int status;

}

