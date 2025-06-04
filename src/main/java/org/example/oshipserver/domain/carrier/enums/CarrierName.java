package org.example.oshipserver.domain.carrier.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CarrierName {
    FEDEX("fedex");
    private final String name;
}
