package org.example.oshipserver.domain.order.dto.bulk;

import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;

public record InternalOrderCreateDto(
    Long sellerId,
    OrderCreateRequest request
) {}
