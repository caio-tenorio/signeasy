package com.example.subscriptions.application.ports;

import com.example.subscriptions.domain.model.plan.Plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepositoryPort {
    Plan save(Plan p);

    Optional<Plan> findByPlanType(String planType);

    Optional<Plan> findById(UUID id);

    List<Plan> listActive();
}