package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.SubscriptionRepositoryPort;
import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.persistence.jpa.SubscriptionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class SubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {
    private final SubscriptionJpaRepository repo;

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository repo) {
        this.repo = repo;
    }

    public Subscription save(Subscription s) {
        return repo.save(s);
    }

    @Override
    public Optional<Subscription> findByIdAndTenantId(UUID id, String tenantId) {
        return repo.findByKeyCustomerIdAndKeyTenantId(id, tenantId);
    }
}