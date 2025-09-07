package com.example.subscriptions.persistence.adapter;

import com.example.subscriptions.application.ports.SubscriptionRepositoryPort;
import com.example.subscriptions.domain.model.Subscription;
import com.example.subscriptions.persistence.jpa.SubscriptionJpaRepository;
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

    public Optional<Subscription> findById(UUID id) {
        return repo.findByKeyId(id);
    }

    public List<Subscription> findByCustomer(UUID customerId) {
        return repo.findByKeyCustomerId(customerId);
    }
}