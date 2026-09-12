package com.example.signeasy.domain.model.plan;

import com.example.signeasy.domain.common.PlanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "plans", uniqueConstraints = @UniqueConstraint(name = "uk_plan_type", columnNames = {"tenant_id", "planType"}))
public class Plan {
    @EmbeddedId
    private PlanKey key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @NotBlank
    private String name;

    @Min(0)
    private int trialDays = 0;

    @Column(nullable = false)
    private boolean active = true;

    public Plan() {}

    public Plan(PlanKey key, PlanType planType, String name, int trialDays, boolean active) {
        this.key = key;
        this.planType = planType;
        this.name = name;
        this.trialDays = trialDays;
        this.active = active;
    }

    public PlanKey getKey() {
        return key;
    }

    public void setKey(PlanKey key) {
        this.key = key;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setTenantId(String tenantId) {
        if (this.key == null) {
            this.key = new PlanKey();
        }
        this.key.setTenantId(tenantId);
    }
}