package com.example.signeasy.application.services;

import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.model.plan.Plan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlanService {
    private final PlanRepositoryPort plans;

    public PlanService(PlanRepositoryPort plans) {
        this.plans = plans;
    }

    public Plan create(Plan p) {
        plans.findByPlanType(p.getPlanType().toString()).ifPresent(x -> {
            throw new BusinessException("Plan code already exists");
        });
        return plans.save(p);
    }

    public List<Plan> listActive() {
        return plans.listActive();
    }
}