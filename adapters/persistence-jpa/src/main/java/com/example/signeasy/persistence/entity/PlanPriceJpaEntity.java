package com.example.signeasy.persistence.entity;

import com.example.signeasy.domain.common.BusinessException;
import com.example.signeasy.domain.common.Period;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "plan_prices", uniqueConstraints = @UniqueConstraint(name = "uk_plan_price_period", columnNames = {"tenant_id", "plan_id", "period"}))
public class PlanPriceJpaEntity {
    @EmbeddedId
    private PlanPriceJpaKey key = new PlanPriceJpaKey();

    // Scalar id owns the foreign-key write; the association shares tenant_id read-only.
    @Column(name = "plan_id", nullable = false, updatable = false)
    private UUID planId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "plan_id", referencedColumnName = "id", insertable = false, updatable = false),
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false)
    })
    private PlanJpaEntity plan;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @Min(0)
    private long priceCents;

    @Column(nullable = false)
    private boolean active = true;

    public PlanPriceJpaEntity() {}

    public PlanPriceJpaEntity(PlanPriceJpaKey key, PlanJpaEntity plan, Period period, long priceCents, boolean active) {
        this.key = key;
        setPlan(plan);
        this.period = period;
        this.priceCents = priceCents;
        this.active = active;
    }

    public PlanPriceJpaKey getKey() {
        return key;
    }

    public void setKey(PlanPriceJpaKey key) {
        this.key = key;
    }

    public PlanJpaEntity getPlan() {
        return plan;
    }

    public void setPlan(PlanJpaEntity plan) {
        requireTenant(plan == null || plan.getKey() == null ? null : plan.getKey().getTenantId());
        this.plan = plan;
        this.planId = plan.getKey().getId();
    }

    private void requireTenant(String tenantId) {
        if (key == null || tenantId == null || !Objects.equals(key.getTenantId(), tenantId)) {
            throw new BusinessException("Plan price and plan must belong to the same tenant");
        }
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(long priceCents) {
        this.priceCents = priceCents;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTenantId(String tenantId) {
        if (this.key == null) {
            this.key = new PlanPriceJpaKey();
        }
        this.key.setTenantId(tenantId);
    }
}
