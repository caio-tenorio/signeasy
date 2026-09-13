package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.CustomerRepositoryPort;
import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.persistence.jpa.CustomerJpaRepository;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.persistence.entity.CustomerJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.CustomerJpaMapper;

import java.util.*;

@Repository
@Transactional
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {
    private final CustomerJpaRepository repo;
    private final CustomerJpaMapper mapper;
    private final EntityManager em;

    public CustomerRepositoryAdapter(CustomerJpaRepository repo, CustomerJpaMapper mapper, EntityManager em) {
        this.repo = repo;
        this.mapper = mapper;
        this.em = em;
    }

    @Override
    public Customer create(Customer customer) {
        var entity = mapper.toEntity(customer);
        em.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Customer update(Customer customer) {
        // Reuses an entity already loaded in the current transaction.
        var entity = em.find(CustomerJpaEntity.class, mapper.toKey(customer.getKey()));
        if (entity == null) {
            throw new BusinessException("Customer not found");
        }
        mapper.updateEntity(customer, entity);
        return mapper.toDomain(entity);
    }

    public Optional<Customer> findByEmailAndTenantId(String email, String tenantId) {
        return repo.findByEmailAndKeyTenantId(email, tenantId).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyIdAndKeyTenantId(id, tenantId).map(mapper::toDomain);
    }
}
