package org.example.oshipserver.domain.auth.dto.response;

import org.example.oshipserver.domain.auth.entity.AuthAddress;

public record AuthAddressResponse(
        String country,

        String city,

        String state,

        String detail1,
        String detail2,
        String zipCode
) {
    public static AuthAddressResponse from(AuthAddress address) {
        if (address == null) return null;

        return new AuthAddressResponse(
                address.getCountry(),
                address.getCity(),
                address.getState(),
                address.getDetail1(),
                address.getDetail2(),
                address.getZipCode()
        );

    }
}
