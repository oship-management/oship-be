package org.example.oshipserver.domain.auth.dto.request;

public record AuthAddressRequest(
        String country,

        String city,

        String state,

        String detail1,
        String detail2,
        String zipCode
) {}
