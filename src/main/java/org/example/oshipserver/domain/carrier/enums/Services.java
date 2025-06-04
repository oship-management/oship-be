package org.example.oshipserver.domain.carrier.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Services {
    IP("FEDEX_INTERNATIONAL_PRIORITY"),
    FICP("FEDEX_INTERNATIONAL_CONNECT_PLUS");
    private final String desc;
}
