package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.persistence.jpa.PlanJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.PlanJpaMapper;

import java.util.*;

@Repository
@Transactional
public class PlanRepositoryAdapter implements PlanRepositoryPort {
    private final PlanJpaRepository repo;
    private final PlanJpaMapper mapper;

    public PlanRepositoryAdapter(PlanJpaRepository repo, PlanJpaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Plan save(Plan p) {
        var entity = repo.findById(mapper.toKey(p.getKey())).orElse(null);
        if (entity == null) {
            entity = mapper.toEntity(p);
        } else {
            mapper.updateEntity(p, entity);
        }
        return mapper.toDomain(repo.saveAndFlush(entity));
    }

    public Optional<Plan> findByPlanTypeAndTenantId(String planType, String tenantId) {
        return repo.findByPlanTypeAndKeyTenantId(PlanType.valueOf(planType), tenantId).map(mapper::toDomain);
    }

    @Override
    public List<Plan> listActivePlansByTenantId(String tenantId) {
        return repo.findByActiveTrueAndKeyTenantId(tenantId).stream().map(mapper::toDomain).toList();
    }
}
