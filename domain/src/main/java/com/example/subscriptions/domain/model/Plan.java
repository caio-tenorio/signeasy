package com.example.subscriptions.domain.model;

import com.example.subscriptions.domain.common.Period;
import com.example.subscriptions.domain.common.PlanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Entity
@Table(name = "plans", uniqueConstraints = @UniqueConstraint(name = "uk_plan_type", columnNames = "type"))
public class Plan {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING) // grava BASIC/PRO/ENTERPRISE no banco
    @Column(nullable = false, unique = true)
    private PlanType planType; // ex.: BASIC, PRO

    @NotBlank
    private String name;

    @Min(0)
    private long priceCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period = Period.MONTHLY;

    @Min(0)
    private int trialDays = 0;

    @Column(nullable = false)
    private boolean active = true;

    // Getters/setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType type) {
        this.planType = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(long priceCents) {
        this.priceCents = priceCents;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int getTrialDays() {
        return trialDays;
    }

    public void setTrialDays(int trialDays) {
        this.trialDays = trialDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}