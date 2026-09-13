package com.example.signeasy.domain.model.plan;

import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

public class PlanPriceKey implements Serializable {

    private UUID id;

    private String tenantId;

    public PlanPriceKey() {}

    public PlanPriceKey(UUID id, String tenantId) {
        this.id = id;
        this.tenantId = tenantId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PlanPriceKey that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() { return Objects.hash(id, tenantId); }
}
