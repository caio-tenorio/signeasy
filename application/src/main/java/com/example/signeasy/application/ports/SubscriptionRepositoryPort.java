package com.example.signeasy.application.ports;

import com.example.signeasy.domain.model.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepositoryPort {
    /** Inserts a new subscription; an existing key must fail. */
    Subscription create(Subscription subscription);

    /** Updates an existing subscription; a missing key must fail. */
    Subscription update(Subscription subscription);
    Optional<Subscription> findByIdAndTenantId(UUID id, String tenantId);
}