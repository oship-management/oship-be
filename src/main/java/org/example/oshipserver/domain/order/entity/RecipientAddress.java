package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.dto.request.OrderUpdateRequest;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.entity.enums.StateCode;
import org.example.oshipserver.domain.order.entity.enums.StateCodeConverter;

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
    @Enumerated(EnumType.STRING)
    private CountryCode recipientCountryCode;

    @Column(name = "recipient_state")
    private String recipientState;

    @Column(name = "recipient_state_code")
    @Convert(converter = StateCodeConverter.class)
    private StateCode recipientStateCode;

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

    public void updateFrom(OrderUpdateRequest req) {
        this.recipientCountryCode = req.recipientCountryCode();
        this.recipientState = req.recipientState();
        this.recipientStateCode = req.recipientStateCode();
        this.recipientCity = req.recipientCity();
        this.recipientAddress1 = req.recipientAddress1();
        this.recipientAddress2 = req.recipientAddress2();
        this.recipientZipCode = req.recipientZipCode();
        this.recipientTaxId = req.recipientTaxId();
    }

}