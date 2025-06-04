package org.example.oshipserver.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;

@Entity
@Table(name = "auth_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 50)
    private String country;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(name = "detail_1", length = 100)
    private String detail1;

    @Column(name = "detail_2", length = 100)
    private String detail2;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    public static AuthAddress from(AuthAddressRequest request, Long userId) {
        if (request == null) return AuthAddress.builder()
                .userId(userId)
                .build();

        return AuthAddress.builder()
                .userId(userId)
                .country(request.country())
                .city(request.city())
                .state(request.state())
                .detail1(request.detail1())
                .detail2(request.detail2())
                .zipCode(request.zipCode())
                .build();
    }
}
