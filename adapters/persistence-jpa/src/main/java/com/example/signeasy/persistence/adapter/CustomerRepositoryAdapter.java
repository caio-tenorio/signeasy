package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.persistence.jpa.CustomerJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.CustomerJpaMapper;

import java.util.*;

@Repository
@Transactional
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final CustomerJpaRepository repo;
    private final CustomerJpaMapper mapper;

    public CustomerRepositoryAdapter(CustomerJpaRepository repo, CustomerJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Customer save(Customer c) {
        var entity = repo.findById(mapper.toKey(c.getKey())).orElse(null);
        if (entity == null) {
            entity = mapper.toEntity(c);
        } else {
            mapper.updateEntity(c, entity);
        }
        return mapper.toDomain(repo.saveAndFlush(entity));
    }

    public Optional<Customer> findByEmailAndTenantId(String email, String tenantId) {
        return repo.findByEmailAndKeyTenantId(email, tenantId).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyIdAndKeyTenantId(id, tenantId).map(mapper::toDomain);
    }
}
