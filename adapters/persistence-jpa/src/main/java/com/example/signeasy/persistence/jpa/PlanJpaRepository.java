package com.example.signeasy.persistence.jpa;

import com.example.signeasy.domain.common.PlanType;
import com.example.signeasy.persistence.entity.PlanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.signeasy.persistence.entity.PlanJpaKey;

import java.util.*;

public interface PlanJpaRepository extends JpaRepository<PlanJpaEntity, PlanJpaKey> {
    Optional<PlanJpaEntity> findByPlanTypeAndKeyTenantId(PlanType planType, String tenantId);

    List<PlanJpaEntity> findByActiveTrueAndKeyTenantId(String tenantId);
}
