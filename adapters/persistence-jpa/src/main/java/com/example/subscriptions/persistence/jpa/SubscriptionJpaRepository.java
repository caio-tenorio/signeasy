package com.example.subscriptions.persistence.jpa;

import com.example.subscriptions.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByCustomerId(UUID customerId);
}
