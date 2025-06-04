package org.example.oshipserver.domain.auth.repository;

import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAddressRepository extends JpaRepository<AuthAddress, Long> {
}
