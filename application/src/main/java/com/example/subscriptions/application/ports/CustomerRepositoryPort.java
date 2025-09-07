package com.example.subscriptions.application.ports;

import com.example.subscriptions.domain.model.customer.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryPort {
    Customer save(Customer c);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByIdAndTenantId(UUID id, String tenantId);
}