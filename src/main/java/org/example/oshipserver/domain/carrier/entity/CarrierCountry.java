package org.example.oshipserver.domain.carrier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String countryCode;
}
