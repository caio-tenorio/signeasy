package com.example.signeasy.application.ports;

import com.example.signeasy.domain.model.customer.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryPort {
    Customer save(Customer c);
    Optional<Customer> findByIdAndTenantId(UUID id, String tenantId);
    Optional<Customer> findByEmailAndTenantId(String email, String tenantId);
}
