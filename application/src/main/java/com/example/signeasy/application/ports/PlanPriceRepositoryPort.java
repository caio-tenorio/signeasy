package com.example.signeasy.application.ports;

import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;

import java.util.List;
import java.util.Optional;

public interface PlanPriceRepositoryPort {
    PlanPrice save(PlanPrice p);

    Optional<PlanPrice> findByPlanTypeAndPeriodAndTenantId(String planType, Period period, String tenantId);

    List<PlanPrice> listActivePricesByTenantId(String tenantId);
}
