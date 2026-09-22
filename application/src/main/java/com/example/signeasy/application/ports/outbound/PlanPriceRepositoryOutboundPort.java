package com.example.signeasy.application.ports.outbound;

import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;

import java.util.List;
import java.util.Optional;

public interface PlanPriceRepositoryOutboundPort {
    /** Inserts a new entity; an existing key must fail. */
    PlanPrice create(PlanPrice price);

    /** Updates an existing entity; a missing key must fail. */
    PlanPrice update(PlanPrice price);

    Optional<PlanPrice> findByPlanTypeAndPeriodAndTenantId(String planType, Period period, String tenantId);

    List<PlanPrice> listActivePricesByTenantId(String tenantId);
}
