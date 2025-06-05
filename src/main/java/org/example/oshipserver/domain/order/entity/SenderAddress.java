package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.Column;
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


@Entity
@Table(name = "sender_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SenderAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_country_code")
    @Enumerated(EnumType.STRING)
    private CountryCode senderCountryCode;

    @Column(name = "sender_state")
    private String senderState;

    @Column(name = "sender_state_code")
    @Enumerated(EnumType.STRING)
    private StateCode senderStateCode;

    @Column(name = "sender_city")
    private String senderCity;

    @Column(name = "sender_address_1")
    private String senderAddress1;

    @Column(name = "sender_address_2")
    private String senderAddress2;

    @Column(name = "sender_zip_code")
    private String senderZipCode;

    @Column(name = "sender_tax_id")
    private String senderTaxId;

    public void updateFrom(OrderUpdateRequest req) {
        this.senderCountryCode = req.senderCountryCode();
        this.senderState = req.senderState();
        this.senderStateCode = req.senderStateCode();
        this.senderCity = req.senderCity();
        this.senderAddress1 = req.senderAddress1();
        this.senderAddress2 = req.senderAddress2();
        this.senderZipCode = req.senderZipCode();
        this.senderTaxId = req.senderTaxId();
    }

}