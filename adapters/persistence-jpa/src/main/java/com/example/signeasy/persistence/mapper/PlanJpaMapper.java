package com.example.signeasy.persistence.mapper;

import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.domain.model.plan.PlanKey;
import com.example.signeasy.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class PlanJpaMapper {

    public PlanJpaKey toKey(PlanKey key) {
        return new PlanJpaKey(key.getId(), key.getTenantId());
    }

    public PlanJpaEntity toEntity(Plan domain) {
        var entity = new PlanJpaEntity();
        entity.setKey(toKey(domain.getKey()));
        updateEntity(domain, entity);
        return entity;
    }

    // Update only business fields; persistence owns identity and audit timestamps.
    public void updateEntity(Plan domain, PlanJpaEntity entity) {

        entity.setPlanType(domain.getPlanType());
        entity.setName(domain.getName());
        entity.setTrialDays(domain.getTrialDays());
        entity.setActive(domain.isActive());
    }

    public Plan toDomain(PlanJpaEntity entity) {
        var key = new PlanKey(entity.getKey().getId(), entity.getKey().getTenantId());
        return new Plan(key, entity.getPlanType(), entity.getName(), entity.getTrialDays(), entity.isActive());
    }
}
