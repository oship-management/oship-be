package org.example.oshipserver.domain.carrier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Table(name = "carrier_countries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarrierCountry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;

    @Column(nullable = false)
    private int zoneNo;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;

    @Builder
    public CarrierCountry(Carrier carrier, int zoneNo, CountryCode countryCode){
        this.carrier = carrier;
        this.zoneNo = zoneNo;
        this.countryCode = countryCode;
    }
}
