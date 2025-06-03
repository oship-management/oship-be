package org.example.oshipserver.domain.seller.dto.request;

public record SellerDeleteRequest(
        String password,
        String passwordValid
) {
}
