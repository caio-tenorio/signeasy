package com.example.subscriptions.persistence.jpa;

import com.example.subscriptions.domain.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByEmailAndKeyTenantId(String email, String tenantId);
    Optional<Customer> findByKeyIdAndKeyTenantId(UUID id, String tenantId);
}
