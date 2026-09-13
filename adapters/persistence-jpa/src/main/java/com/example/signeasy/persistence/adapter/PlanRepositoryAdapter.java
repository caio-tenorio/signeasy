package com.example.signeasy.persistence.adapter;

import com.example.signeasy.application.ports.PlanRepositoryPort;
import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import com.example.signeasy.persistence.jpa.PlanJpaRepository;
import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.persistence.entity.PlanJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.signeasy.persistence.mapper.PlanJpaMapper;

import java.util.*;

@Repository
@Transactional
public class PlanRepositoryAdapter implements PlanRepositoryPort {
    private final PlanJpaRepository repo;
    private final PlanJpaMapper mapper;
    private final EntityManager em;

    public PlanRepositoryAdapter(PlanJpaRepository repo, PlanJpaMapper mapper, EntityManager em) {
        this.repo = repo;
        this.mapper = mapper;
        this.em = em;
    }

    @Override
    public Plan create(Plan plan) {
        var entity = mapper.toEntity(plan);
        em.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Plan update(Plan plan) {
        // Reuses an entity already loaded in the current transaction.
        var entity = em.find(PlanJpaEntity.class, mapper.toKey(plan.getKey()));
        if (entity == null) {
            throw new BusinessException("Plan not found");
        }
        mapper.updateEntity(plan, entity);
        return mapper.toDomain(entity);
    }

    public Optional<Plan> findByPlanTypeAndTenantId(String planType, String tenantId) {
        return repo.findByPlanTypeAndKeyTenantId(PlanType.valueOf(planType), tenantId).map(mapper::toDomain);
    }

    @Override
    public List<Plan> listActivePlansByTenantId(String tenantId) {
        return repo.findByActiveTrueAndKeyTenantId(tenantId).stream().map(mapper::toDomain).toList();
    }
}
