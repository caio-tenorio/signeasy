package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.PlanPriceRepositoryPort;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.persistence.jpa.PlanPriceJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PlanPriceRepositoryAdapter implements PlanPriceRepositoryPort {
    private final PlanPriceJpaRepository repo;

    public PlanPriceRepositoryAdapter(PlanPriceJpaRepository repo) {
        this.repo = repo;
    }

    public PlanPrice save(PlanPrice p) {
        return repo.save(p);
    }

    @Override
    public Optional<PlanPrice> findByPlanTypeAndPeriodAndTenantId(String planType, Period period, String tenantId) {
        return repo.findByPlan_PlanTypeAndPeriodAndKeyTenantId(PlanType.valueOf(planType), period, tenantId);
    }

    @Override
    public List<PlanPrice> listActivePricesByTenantId(String tenantId) {
        return repo.findByActiveTrueAndKeyTenantId(tenantId);
    }
}
