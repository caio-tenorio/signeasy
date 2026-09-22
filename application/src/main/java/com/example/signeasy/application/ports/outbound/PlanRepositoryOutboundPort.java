package com.example.signeasy.application.ports.outbound;

import com.example.signeasy.domain.model.plan.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepositoryOutboundPort {
    /** Inserts a new entity; an existing key must fail. */
    Plan create(Plan plan);

    /** Updates an existing entity; a missing key must fail. */
    Plan update(Plan plan);

    Optional<Plan> findByPlanTypeAndTenantId(String planType, String tenantId);

    List<Plan> listActivePlansByTenantId(String tenantId);
}
