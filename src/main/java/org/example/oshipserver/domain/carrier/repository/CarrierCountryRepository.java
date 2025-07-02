package org.example.oshipserver.domain.carrier.repository;

import org.example.oshipserver.domain.carrier.entity.CarrierCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierCountryRepository extends JpaRepository<CarrierCountry, Long> {

    boolean existsByZoneNoAndCarrierId(int zoneNo, Long carrierId);
}
