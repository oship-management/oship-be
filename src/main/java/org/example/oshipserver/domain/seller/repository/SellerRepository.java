package org.example.oshipserver.domain.seller.repository;

import org.example.oshipserver.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long>, ISellerRepository {

}
