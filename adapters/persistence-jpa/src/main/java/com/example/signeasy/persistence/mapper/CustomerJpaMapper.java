package com.example.signeasy.persistence.mapper;

import com.example.signeasy.domain.model.customer.Customer;
import com.example.signeasy.domain.model.customer.CustomerKey;
import com.example.signeasy.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class CustomerJpaMapper {

    public CustomerJpaKey toKey(CustomerKey key) {
        return new CustomerJpaKey(key.getId(), key.getTenantId());
    }

    public CustomerJpaEntity toEntity(Customer domain) {
        var entity = new CustomerJpaEntity();
        entity.setKey(toKey(domain.getKey()));
        updateEntity(domain, entity);
        return entity;
    }

    // Update only business fields; persistence owns identity and audit timestamps.
    public void updateEntity(Customer domain, CustomerJpaEntity entity) {

        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setStatus(domain.getStatus());
    }

    public Customer toDomain(CustomerJpaEntity entity) {
        var key = new CustomerKey(entity.getKey().getId(), entity.getKey().getTenantId());
        return new Customer(key, entity.getName(), entity.getEmail(), entity.getStatus());
    }
}
