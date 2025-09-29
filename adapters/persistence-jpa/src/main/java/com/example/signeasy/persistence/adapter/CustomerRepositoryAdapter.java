package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.persistence.jpa.CustomerJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final CustomerJpaRepository repo;

    public CustomerRepositoryAdapter(CustomerJpaRepository repo) {
        this.repo = repo;
    }

    public Customer save(Customer c) {
        return repo.save(c);
    }

    public Optional<Customer> findByEmailAndTenantId(String email, String tenantId) {
        return repo.findByEmailAndKeyTenantId(email, tenantId);
    }

    @Override
    public Optional<Customer> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyIdAndKeyTenantId(id, tenantId);
    }
}