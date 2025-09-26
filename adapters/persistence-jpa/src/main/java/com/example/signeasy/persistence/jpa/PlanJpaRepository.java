package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.domain.model.plan.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanJpaRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByPlanTypeAndKeyTenantId(PlanType planType, String tenantId);

    List<Plan> findByActiveTrueAndKeyTenantId(String tenantId);
}
