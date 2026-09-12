package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.SubscriptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, SubscriptionKey> {
    @EntityGraph(attributePaths = {"customer", "planPrice", "planPrice.plan"})
    Optional<Subscription> findByKeyIdAndKeyTenantId(UUID id, String tenantId);
}
