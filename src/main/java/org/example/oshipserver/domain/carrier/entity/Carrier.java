package org.example.oshipserver.domain.carrier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.carrier.enums.CarrierName;
import org.example.oshipserver.domain.carrier.enums.Services;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Table(name = "carriers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carrier extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @Column(length = 255, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CarrierName name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private BigDecimal weightMin;

    @Column(nullable = false)
    private BigDecimal weightMax;

    @Column(length = 50, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Services service;

    @Column(length = 255, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expired;
}
