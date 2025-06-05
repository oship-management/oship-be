package org.example.oshipserver.domain.carrier.repository;

import org.example.oshipserver.domain.carrier.entity.CarrierRateCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRateChargeRepository extends JpaRepository<CarrierRateCharge, Long> {

}
