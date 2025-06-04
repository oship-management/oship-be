package org.example.oshipserver.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.oshipserver.domain.order.entity.enums.DeleterRole;

public record OrderDeleteRequest(
    @NotNull(message = "삭제 주체는 필수입니다.")
    DeleterRole deletedBy
) {}
