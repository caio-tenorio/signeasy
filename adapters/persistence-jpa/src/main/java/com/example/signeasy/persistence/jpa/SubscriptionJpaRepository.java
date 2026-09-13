package com.example.signeasy.persistence.jpa;

import com.example.signeasy.persistence.entity.SubscriptionJpaEntity;
import com.example.signeasy.persistence.entity.SubscriptionJpaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, SubscriptionJpaKey> {
    @EntityGraph(attributePaths = {"customer", "planPrice", "planPrice.plan"})
    Optional<SubscriptionJpaEntity> findByKeyIdAndKeyTenantId(UUID id, String tenantId);
}
