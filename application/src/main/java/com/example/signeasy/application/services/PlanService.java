package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.inbound.PlanInboundPort;
import com.example.signeasy.application.ports.outbound.PlanRepositoryOutboundPort;
import com.example.signeasy.application.ports.outbound.TenantContextOutboundPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.plan.Plan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlanService implements PlanInboundPort {
    private final PlanRepositoryOutboundPort planRepositoryPort;
    private final TenantContextOutboundPort tenantContext;

    public PlanService(PlanRepositoryOutboundPort planRepositoryPort, TenantContextOutboundPort tenantContext) {
        this.planRepositoryPort = planRepositoryPort;
        this.tenantContext = tenantContext;
    }

    @Override
    public Plan create(Plan plan) {
        plan.setTenantId(tenantContext.currentTenantId());
        if (plan.getKey().getId() == null) {
            plan.getKey().setId(UUID.randomUUID());
        }
        planRepositoryPort.findByPlanTypeAndTenantId(plan.getPlanType().toString(), plan.getKey().getTenantId()).ifPresent(x -> {
            throw new BusinessException("Plan code already exists");
        });
        return planRepositoryPort.create(plan);
    }

    @Override
    public List<Plan> listActive() {
        return planRepositoryPort.listActivePlansByTenantId(tenantContext.currentTenantId());
    }
}
