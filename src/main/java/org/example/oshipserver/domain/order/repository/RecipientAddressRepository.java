package org.example.oshipserver.domain.order.repository;

import org.example.oshipserver.domain.order.entity.RecipientAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipientAddressRepository extends JpaRepository<RecipientAddress, Long> {

}
