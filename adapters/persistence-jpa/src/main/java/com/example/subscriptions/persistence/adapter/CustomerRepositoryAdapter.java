package com.example.subscriptions.persistence.adapter;

import com.example.subscriptions.application.ports.CustomerRepositoryPort;
import com.example.subscriptions.domain.model.customer.Customer;
import com.example.subscriptions.persistence.jpa.CustomerJpaRepository;
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

    public Optional<Customer> findById(UUID id) {
        return repo.findById(id);
    }

    public Optional<Customer> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public Optional<Customer> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyIdAndKeyTenantId(id, tenantId);
    }
}