package com.example.signeasy.persistence.jpa;

import com.example.signeasy.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.signeasy.persistence.entity.CustomerJpaKey;

import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, CustomerJpaKey> {
    Optional<CustomerJpaEntity> findByEmailAndKeyTenantId(String email, String tenantId);
    Optional<CustomerJpaEntity> findByKeyIdAndKeyTenantId(UUID id, String tenantId);
}
