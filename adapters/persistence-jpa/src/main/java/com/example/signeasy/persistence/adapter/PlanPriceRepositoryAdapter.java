package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.PlanPriceRepositoryPort;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.persistence.jpa.PlanPriceJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.PlanPriceJpaMapper;

import java.util.*;

@Repository
@Transactional
public class PlanPriceRepositoryAdapter implements PlanPriceRepositoryPort {
    private final PlanPriceJpaRepository repo;
    private final PlanPriceJpaMapper mapper;

    public PlanPriceRepositoryAdapter(PlanPriceJpaRepository repo, PlanPriceJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public PlanPrice save(PlanPrice p) {
        var entity = repo.findById(mapper.toKey(p.getKey())).orElse(null);
        if (entity == null) {
            entity = mapper.toEntity(p);
        } else {
            mapper.updateEntity(p, entity);
        }
        return mapper.toDomain(repo.saveAndFlush(entity));
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
