package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.outbound.PlanPriceRepositoryOutboundPort;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.persistence.jpa.PlanPriceJpaRepository;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.persistence.entity.PlanPriceJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.PlanPriceJpaMapper;

import java.util.*;

@Repository
@Transactional
public class PlanPriceRepositoryAdapter implements PlanPriceRepositoryOutboundPort {
    private final PlanPriceJpaRepository repo;
    private final PlanPriceJpaMapper mapper;
    private final EntityManager em;

    public PlanPriceRepositoryAdapter(PlanPriceJpaRepository repo, PlanPriceJpaMapper mapper, EntityManager em) {
        this.repo = repo;
        this.mapper = mapper;
        this.em = em;
    }

    @Override
    public PlanPrice create(PlanPrice price) {
        var entity = mapper.toEntity(price);
        em.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public PlanPrice update(PlanPrice price) {
        // Reuses an entity already loaded in the current transaction.
        var entity = em.find(PlanPriceJpaEntity.class, mapper.toKey(price.getKey()));
        if (entity == null) {
            throw new BusinessException("Plan price not found");
        }
        mapper.updateEntity(price, entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<PlanPrice> findByPlanTypeAndPeriodAndTenantId(String planType, Period period, String tenantId) {
        return repo.findByPlan_PlanTypeAndPeriodAndKeyTenantId(PlanType.valueOf(planType), period, tenantId).map(mapper::toDomain);
    }

    @Override
    public List<PlanPrice> listActivePricesByTenantId(String tenantId) {
        return repo.findByActiveTrueAndKeyTenantId(tenantId).stream().map(mapper::toDomain).toList();
    }
}
