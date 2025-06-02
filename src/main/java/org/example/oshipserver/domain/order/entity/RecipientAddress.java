package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "recipient_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class RecipientAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String countryCode;
    private String state;
    private String stateCode;
    private String city;
    private String address1;
    private String address2;
    private String zipCode;
    private String taxId;
}
