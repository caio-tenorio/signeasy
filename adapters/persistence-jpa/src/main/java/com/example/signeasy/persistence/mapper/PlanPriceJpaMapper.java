package com.example.signeasy.persistence.mapper;

import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.domain.model.plan.PlanPriceKey;
import com.example.signeasy.persistence.entity.*;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;

@Component
public class PlanPriceJpaMapper {
    private final EntityManager em;
    private final PlanJpaMapper planMapper;

    public PlanPriceJpaMapper(EntityManager em, PlanJpaMapper planMapper) {
        this.em = em;
        this.planMapper = planMapper;
    }

    public PlanPriceJpaKey toKey(PlanPriceKey key) {
        return new PlanPriceJpaKey(key.getId(), key.getTenantId());
    }

    public PlanPriceJpaEntity toEntity(PlanPrice domain) {
        var entity = new PlanPriceJpaEntity();
        entity.setKey(toKey(domain.getKey()));
        updateEntity(domain, entity);
        return entity;
    }

    // Update only business fields; persistence owns identity and audit timestamps.
    public void updateEntity(PlanPrice domain, PlanPriceJpaEntity entity) {
        entity.setPlan(em.getReference(PlanJpaEntity.class,
                planMapper.toKey(domain.getPlan().getKey())));
        entity.setPeriod(domain.getPeriod());
        entity.setPriceCents(domain.getPriceCents());
        entity.setActive(domain.isActive());
    }

    public PlanPrice toDomain(PlanPriceJpaEntity entity) {
        var key = new PlanPriceKey(entity.getKey().getId(), entity.getKey().getTenantId());
        return new PlanPrice(key, planMapper.toDomain(entity.getPlan()), entity.getPeriod(), entity.getPriceCents(), entity.isActive());
    }
}
