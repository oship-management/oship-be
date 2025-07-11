package org.example.oshipserver.domain.partner.repository;

import org.example.oshipserver.domain.partner.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long>, IPartnerRepository {

    Optional<Partner> findByUserId(Long userId);

    boolean existsByCompanyRegisterNo(String companyRegisterNo);
}
