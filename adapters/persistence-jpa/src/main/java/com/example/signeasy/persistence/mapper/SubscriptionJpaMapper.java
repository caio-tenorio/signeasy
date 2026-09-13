package com.example.signeasy.persistence.mapper;

import com.example.signeasy.domain.model.Subscription;
import com.example.signeasy.domain.model.SubscriptionKey;
import com.example.signeasy.persistence.entity.*;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;

@Component
public class SubscriptionJpaMapper {
    private final EntityManager em;
    private final CustomerJpaMapper customerMapper;
    private final PlanPriceJpaMapper planPriceMapper;

    public SubscriptionJpaMapper(EntityManager em, CustomerJpaMapper customerMapper, PlanPriceJpaMapper planPriceMapper) {
        this.em = em;
        this.customerMapper = customerMapper;
        this.planPriceMapper = planPriceMapper;
    }

    public SubscriptionJpaKey toKey(SubscriptionKey key) {
        return new SubscriptionJpaKey(key.getId(), key.getTenantId());
    }

    public SubscriptionJpaEntity toEntity(Subscription domain) {
        var entity = new SubscriptionJpaEntity();
        entity.setKey(toKey(domain.getKey()));
        updateEntity(domain, entity);
        return entity;
    }

    // Update only business fields; persistence owns identity and audit timestamps.
    public void updateEntity(Subscription domain, SubscriptionJpaEntity entity) {
        entity.setCustomer(em.getReference(CustomerJpaEntity.class,
                customerMapper.toKey(domain.getCustomer().getKey())));
        entity.setPlanPrice(em.getReference(PlanPriceJpaEntity.class,
                planPriceMapper.toKey(domain.getPlanPrice().getKey())));
        entity.setStatus(domain.getStatus());
        entity.setStartDate(domain.getStartDate());
        entity.setTrialEndDate(domain.getTrialEndDate());
        entity.setNextBillingDate(domain.getNextBillingDate());
        entity.setEndDate(domain.getEndDate());
    }

    public Subscription toDomain(SubscriptionJpaEntity entity) {
        var key = new SubscriptionKey(entity.getKey().getId(), entity.getKey().getTenantId());
        return new Subscription(key, customerMapper.toDomain(entity.getCustomer()), planPriceMapper.toDomain(entity.getPlanPrice()), entity.getStatus(), entity.getStartDate(), entity.getTrialEndDate(), entity.getNextBillingDate(), entity.getEndDate());
    }
}
