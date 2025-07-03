package org.example.oshipserver.domain.seller.repository;

import java.util.Optional;
import org.example.oshipserver.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long>, ISellerRepository {

    boolean existsByCompanyRegisterNo(String companyRegisterNo);

    // sellerId와 userId 연관관계 수정 전, 임시 코드
    Optional<Seller> findByUserId(Long userId);

}
