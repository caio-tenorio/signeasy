package com.example.signeasy.application.ports;

import com.example.signeasy.domain.model.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepositoryPort {
    Subscription save(Subscription s);

    Optional<Subscription> findById(UUID id);

    List<Subscription> findByCustomer(UUID customerId);
}