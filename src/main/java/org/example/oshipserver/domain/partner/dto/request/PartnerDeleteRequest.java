package org.example.oshipserver.domain.partner.dto.request;

public record PartnerDeleteRequest(
        String password,
        String passwordValid
) {
}
