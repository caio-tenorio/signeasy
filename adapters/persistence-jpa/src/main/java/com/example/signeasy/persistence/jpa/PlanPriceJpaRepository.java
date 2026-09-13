package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.persistence.entity.PlanPriceJpaEntity;
import com.example.signeasy.persistence.entity.PlanPriceJpaKey;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanPriceJpaRepository extends JpaRepository<PlanPriceJpaEntity, PlanPriceJpaKey> {
    @EntityGraph(attributePaths = {"plan"})
    Optional<PlanPriceJpaEntity> findByPlan_PlanTypeAndPeriodAndKeyTenantId(PlanType planType, Period period, String tenantId);

    @EntityGraph(attributePaths = {"plan"})
    List<PlanPriceJpaEntity> findByActiveTrueAndKeyTenantId(String tenantId);
}
