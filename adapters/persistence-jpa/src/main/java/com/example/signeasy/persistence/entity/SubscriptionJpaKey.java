package com.example.signeasy.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubscriptionJpaKey implements Serializable {
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 30)
    private String tenantId;

    public SubscriptionJpaKey() {}

    public SubscriptionJpaKey(UUID id, String tenantId) {
        this.id = id;
        this.tenantId = tenantId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SubscriptionJpaKey that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() { return Objects.hash(id, tenantId); }
}
