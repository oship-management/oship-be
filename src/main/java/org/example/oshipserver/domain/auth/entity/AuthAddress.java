package org.example.oshipserver.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "auth_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicUpdate
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
    public void update(AuthAddressRequest request) {
        if (request == null) return;

        if (request.country() != null) this.country = request.country();
        if (request.city() != null) this.city = request.city();
        if (request.state() != null) this.state = request.state();
        if (request.detail1() != null) this.detail1 = request.detail1();
        if (request.detail2() != null) this.detail2 = request.detail2();
        if (request.zipCode() != null) this.zipCode = request.zipCode();
    }
}
