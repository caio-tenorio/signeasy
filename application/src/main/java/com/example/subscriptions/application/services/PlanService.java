package com.example.subscriptions.application.services;

import com.example.subscriptions.application.ports.PlanRepositoryPort;
import com.example.subscriptions.domain.common.BusinessException;
import com.example.subscriptions.domain.model.Plan;
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
        plans.findByCode(p.getCode()).ifPresent(x -> {
            throw new BusinessException("Plan code already exists");
        });
        return plans.save(p);
    }

    public List<Plan> listActive() {
        return plans.listActive();
    }
}