package com.example.subscriptions.persistence.adapter;

import com.example.subscriptions.application.ports.PlanRepositoryPort;
import com.example.subscriptions.domain.model.Plan;
import com.example.subscriptions.persistence.jpa.PlanJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PlanRepositoryAdapter implements PlanRepositoryPort {
    private final PlanJpaRepository repo;

    public PlanRepositoryAdapter(PlanJpaRepository repo) {
        this.repo = repo;
    }

    public Plan save(Plan p) {
        return repo.save(p);
    }

    public Optional<Plan> findByCode(String code) {
        return repo.findByCode(code);
    }

    public Optional<Plan> findById(UUID id) {
        return repo.findById(id);
    }

    public List<Plan> listActive() {
        return repo.findByActiveTrue();
    }
}