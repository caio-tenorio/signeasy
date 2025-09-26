package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.persistence.jpa.PlanJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PlanRepositoryAdapter implements PlanRepositoryPort {
    private final PlanJpaRepository repo;

    public PlanRepositoryAdapter(PlanJpaRepository repo) {
        this.repo = repo;
    }

    public Plan save(Plan p) {
        return repo.save(p);
    }

    public Optional<Plan> findByPlanTypeAndTenantId(String planType, String tenantId) {
        return repo.findByPlanTypeAndKeyTenantId(PlanType.valueOf(planType), tenantId);
    }

    @Override
    public List<Plan> listActivePlansByTenantId(String tenantId) {
        return repo.findByActiveTrueAndKeyTenantId(tenantId);
    }
}