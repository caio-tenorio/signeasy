package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.common.Period;
import com.example.signeasy.domain.model.plan.PlanPrice;
import com.example.signeasy.domain.model.plan.PlanPriceKey;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanPriceJpaRepository extends JpaRepository<PlanPrice, PlanPriceKey> {
    @EntityGraph(attributePaths = {"plan"})
    Optional<PlanPrice> findByPlan_PlanTypeAndPeriodAndKeyTenantId(PlanType planType, Period period, String tenantId);

    @EntityGraph(attributePaths = {"plan"})
    List<PlanPrice> findByActiveTrueAndKeyTenantId(String tenantId);
}
