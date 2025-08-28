package com.example.subscriptions.persistence.jpa;

import com.example.subscriptions.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlanJpaRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByCode(String code);

    List<Plan> findByActiveTrue();
}
