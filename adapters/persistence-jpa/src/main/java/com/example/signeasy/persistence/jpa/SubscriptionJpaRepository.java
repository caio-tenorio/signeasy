package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.SubscriptionKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, SubscriptionKey> {
    List<Subscription> findByKeyCustomerId(UUID customerId);
    Optional<Subscription> findByKeyId(UUID id);
}
