package org.example.oshipserver.domain.seller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SellerDeleteRequest(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,
        @NotBlank(message = "비밀번호를 확인해 주세요.")
        String passwordValid
) {
}
