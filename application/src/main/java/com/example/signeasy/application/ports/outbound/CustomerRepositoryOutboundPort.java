package com.example.signeasy.application.ports.outbound;

import com.example.signeasy.domain.model.customer.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryOutboundPort {
    /** Inserts a new entity; an existing key must fail. */
    Customer create(Customer customer);

    /** Updates an existing entity; a missing key must fail. */
    Customer update(Customer customer);
    Optional<Customer> findByIdAndTenantId(UUID id, String tenantId);
    Optional<Customer> findByEmailAndTenantId(String email, String tenantId);
}
