package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.model.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.signeasy.domain.model.customer.CustomerKey;

import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<Customer, CustomerKey> {
    Optional<Customer> findByEmailAndKeyTenantId(String email, String tenantId);
    Optional<Customer> findByKeyIdAndKeyTenantId(UUID id, String tenantId);
}
