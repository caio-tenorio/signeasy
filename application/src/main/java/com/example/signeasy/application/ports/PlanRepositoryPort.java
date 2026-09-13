package com.example.signeasy.application.ports;

import com.example.signeasy.domain.model.plan.Plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepositoryPort {
    /** Inserts a new entity; an existing key must fail. */
    Plan create(Plan plan);

    /** Updates an existing entity; a missing key must fail. */
    Plan update(Plan plan);

    Optional<Plan> findByPlanTypeAndTenantId(String planType, String tenantId);

    List<Plan> listActivePlansByTenantId(String tenantId);
}