package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.application.ports.TenantContext;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.plan.Plan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlanService {
    private final PlanRepositoryPort planRepositoryPort;
    private final TenantContext tenantContext;

    public PlanService(PlanRepositoryPort planRepositoryPort, TenantContext tenantContext) {
        this.planRepositoryPort = planRepositoryPort;
        this.tenantContext = tenantContext;
    }

    public Plan create(Plan plan) {
        plan.setTenantId(tenantContext.currentTenantId());
        if (plan.getKey().getId() == null) {
            plan.getKey().setId(UUID.randomUUID());
        }
        planRepositoryPort.findByPlanTypeAndTenantId(plan.getPlanType().toString(), plan.getKey().getTenantId()).ifPresent(x -> {
            throw new BusinessException("Plan code already exists");
        });
        return planRepositoryPort.save(plan);
    }

    public List<Plan> listActive() {
        return planRepositoryPort.listActivePlansByTenantId(tenantContext.currentTenantId());
    }
}
