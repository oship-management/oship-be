package org.example.oshipserver.domain.admin.dto.response;

import lombok.Builder;

@Builder
public record ResponseRateDto(
    long success,
    long fail
) {

}
