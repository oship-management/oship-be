package org.example.oshipserver.domain.partner.repository;

import org.example.oshipserver.domain.partner.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<Partner, Long>, IPartnerRepository {


}
