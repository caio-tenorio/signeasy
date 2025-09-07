package com.example.signeasy.application.ports;

import com.example.signeasy.domain.model.plan.Plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepositoryPort {
    Plan save(Plan p);

    Optional<Plan> findByPlanType(String planType);

    Optional<Plan> findById(UUID id);

    List<Plan> listActive();
}