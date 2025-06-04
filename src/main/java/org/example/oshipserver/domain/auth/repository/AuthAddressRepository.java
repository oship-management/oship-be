package org.example.oshipserver.domain.auth.repository;

import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthAddressRepository extends JpaRepository<AuthAddress, Long> {
    Optional<AuthAddress> findByUserId(Long userId);
}
