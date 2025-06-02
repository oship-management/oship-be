package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipient_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RecipientAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_country_code")
    private String recipientCountryCode;

    @Column(name = "recipient_state")
    private String recipientState;

    @Column(name = "recipient_state_code")
    private String recipientStateCode;

    @Column(name = "recipient_city")
    private String recipientCity;

    @Column(name = "recipient_address_1")
    private String recipientAddress1;

    @Column(name = "recipient_address_2")
    private String recipientAddress2;

    @Column(name = "recipient_zip_code")
    private String recipientZipCode;

    @Column(name = "recipient_tax_id")
    private String recipientTaxId;
}
